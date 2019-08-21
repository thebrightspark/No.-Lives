package brightspark.nolives.command;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

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
		ITextComponent message = NoLives.newMessageText("lives", lives, NoLives.lifeOrLives(lives));
		if (NLConfig.maxLives > 0)
			message.appendSibling(NoLives.newMessageText("lives.max", NLConfig.maxLives));
		sender.sendMessage(message);
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
		private static final int MAX_PAGE_SIZE = 8;

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
			// Get list page num if provided
			int page = -1;
			if (args.length > 0) {
				try {
					page = Integer.parseInt(args[0]);
				} catch (NumberFormatException ignored) { }
			}
			page = Math.max(0, page - 1);

			Map<UUID, PlayerLives> livesMap = getLivesData(sender).getAllLives();
			int livesSize = livesMap.size();
			int pageMax = livesSize / (MAX_PAGE_SIZE + 1);
			if (page * MAX_PAGE_SIZE > livesSize)
				page = pageMax;

			// Gets the data to display for the page
			PlayerProfileCache cache = server.getPlayerProfileCache();
			List<Pair<String, Integer>> data = livesMap.entrySet().stream().unordered()
				.map(entry -> {
					GameProfile profile = cache.getProfileByUUID(entry.getKey());
					if (profile == null) return null;
					return Pair.of(profile.getName(), entry.getValue().lives);
				})
				.filter(Objects::nonNull)
				.sorted((o1, o2) -> {
					// Reverse sort
					int comparison = Integer.compare(o1.getRight(), o2.getRight());
					if (comparison != 0) comparison *= -1;
					return comparison;
				})
				.skip(page * MAX_PAGE_SIZE)
				.limit(MAX_PAGE_SIZE)
				.collect(Collectors.toList());

			if (data.isEmpty()) {
				// No data... this shouldn't happen!
				throw new CommandException("nolives.command.lives.list.fail.data");
			}

			// Get the longest lives
			int longestLives = Integer.toString(data.stream().mapToInt(Pair::getRight).max().getAsInt()).length();

			// Create the message
			ITextComponent message = NoLives.newMessageText("lives.list.title");
			message.getStyle().setColor(TextFormatting.GOLD);
			data.forEach(triple -> {
				String lives = triple.getRight().toString();
				message.appendText("\n" + TextFormatting.RED + genWhitespace(longestLives - lives.length()) + lives)
					.appendText(TextFormatting.WHITE + " - ")
					.appendText(TextFormatting.YELLOW + triple.getLeft());
			});
			message.appendText("\n").appendSibling(NoLives.newMessageText("lives.list.page", page + 1, pageMax + 1));

			sender.sendMessage(message);
		}

		private String genWhitespace(int length) {
			if (length <= 0)
				return "";
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
			NoLives.sendMessageText(sender, "lives.add", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount));
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
			NoLives.sendMessageText(sender, "lives.sub", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount));
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
			NoLives.sendMessageText(sender, "lives.set", newAmount, NoLives.lifeOrLives(newAmount), targetPlayer.getRight());
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}
}
