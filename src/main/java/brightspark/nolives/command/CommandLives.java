package brightspark.nolives.command;

import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class CommandLives extends CommandTreeBase {
	public CommandLives() {
		// TODO: Create a GUI for the list instead? Use "/lives" to open the GUI
		//  It can also show the current player's lives at the top too
		//  Maybe even a search bar too?
		addSubcommand(new CommandList());
		addSubcommand(new CommandAdd());
		addSubcommand(new CommandSub());
		addSubcommand(new CommandSet());
		addSubcommand(new CommandTreeHelp(this));
	}

	@Override
	public String getName() {
		return "lives";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "nolives.command.lives.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0)
			showLives(sender);
		else if (server.isHardcore())
			NoLives.sendMessageText(sender, "lives.hardcore");
		else
			super.execute(server, sender, args);
	}

	private void showLives(ICommandSender sender) throws CommandException {
		if (!(sender instanceof EntityPlayer))
			throw new CommandException("nolives.command.lives.fail.player");
		EntityPlayer player = (EntityPlayer) sender;
		PlayerLivesWorldData livesData = getLivesData(player);
		int lives = livesData.getLives(player.getUniqueID());
		NoLives.sendMessageText(sender, "lives", lives, NoLives.lifeOrLives(lives));
	}

	private PlayerLivesWorldData getLivesData(ICommandSender sender) throws CommandException {
		PlayerLivesWorldData livesData = PlayerLivesWorldData.get(sender.getEntityWorld());
		if (livesData == null) throw new CommandException("nolives.command.lives.fail.data");
		return livesData;
	}

	private Pair<UUID, String> getPlayerUuidAndName(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		UUID uuidToChange;
		String playerName;
		if (args.length >= 2) {
			String target = args[0];
			try {
				EntityPlayer player = getPlayer(server, sender, target);
				uuidToChange = player.getUniqueID();
				playerName = player.getName();
			} catch (PlayerNotFoundException e) {
				GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(target);
				if (profile == null)
					throw e;
				else {
					uuidToChange = profile.getId();
					playerName = profile.getName();
				}
			}
		} else {
			EntityPlayer player = getCommandSenderAsPlayer(sender);
			uuidToChange = player.getUniqueID();
			playerName = player.getName();
		}
		return Pair.of(uuidToChange, playerName);
	}

	private int getAmount(String[] args) throws CommandException {
		//Get amount argument
		return parseInt(args[args.length >= 2 ? 1 : 0]);
	}

	private List<String> getPlayerNameTabCompletions(MinecraftServer server, String[] args) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames()) : Collections.emptyList();
	}

	private class CommandList extends CommandBase {
		@Override
		public String getName() {
			return "list";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "lives list";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 0;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			PlayerLivesWorldData livesData = getLivesData(sender);

			ITextComponent text = NoLives.newMessageText("lives.list");
			PlayerProfileCache cache = server.getPlayerProfileCache();
			List<String> playerNames = Lists.newArrayList(cache.getUsernames());
			int longestName = 0;
			for (String name : playerNames)
				if (name.length() > longestName)
					longestName = name.length();
			longestName += 5;
			Map<UUID, PlayerLives> allLives = livesData.getAllLives();
			for (Map.Entry<UUID, PlayerLives> entry : allLives.entrySet()) {
				UUID uuid = entry.getKey();
				GameProfile profile = cache.getProfileByUUID(uuid);
				if (profile == null) continue;
				String name = profile.getName();
				String entryLives = String.valueOf(entry.getValue().lives);
				String whitespace = genWhitespace(longestName - entryLives.length());
				text.appendText("\n").appendText(name).appendText(whitespace).appendText(entryLives);
			}
			sender.sendMessage(text);
		}

		private String genWhitespace(int length) {
			StringBuilder sb = new StringBuilder(length);
			for (int i = 0; i < length; i++)
				sb.append(" ");
			return sb.toString();
		}
	}

	private class CommandAdd extends CommandBase {
		@Override
		public String getName() {
			return "add";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.add.usage";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 2;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			PlayerLivesWorldData livesData = getLivesData(sender);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args);
			int amount = getAmount(args);
			int newAmount = livesData.addLives(targetPlayer.getLeft(), amount);
			sender.sendMessage(NoLives.newMessageText("lives.add", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount)));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}

	private class CommandSub extends CommandBase {
		@Override
		public String getName() {
			return "sub";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.sub.usage";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 2;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			PlayerLivesWorldData livesData = getLivesData(sender);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args);
			int amount = getAmount(args);
			int newAmount = livesData.subLives(targetPlayer.getLeft(), amount);
			sender.sendMessage(NoLives.newMessageText("lives.sub", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount)));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}

	private class CommandSet extends CommandBase {
		@Override
		public String getName() {
			return "set";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.set.usage";
		}

		@Override
		public int getRequiredPermissionLevel() {
			return 2;
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			PlayerLivesWorldData livesData = getLivesData(sender);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args);
			int amount = getAmount(args);
			int newAmount = livesData.setLives(targetPlayer.getLeft(), amount);
			sender.sendMessage(NoLives.newMessageText("lives.set", newAmount, NoLives.lifeOrLives(newAmount), targetPlayer.getRight()));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}
}
