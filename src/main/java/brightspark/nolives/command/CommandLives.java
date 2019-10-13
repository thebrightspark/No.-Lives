package brightspark.nolives.command;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
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
		addSubcommand(new CommandMax());
		addSubcommand(new CommandRevive());
		addSubcommand(new CommandEnable());
		addSubcommand(new CommandDisable());
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
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return NoLives.checkCommandPermission(this, server, server);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			showLives(sender);
		}
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
			message.appendSibling(NoLives.newMessageText("maxlives", NLConfig.maxLives));
		sender.sendMessage(message);
	}

	private PlayerLivesWorldData getLivesData(ICommandSender sender) throws CommandException {
		PlayerLivesWorldData livesData = PlayerLivesWorldData.get(sender.getEntityWorld());
		if (livesData == null) throw new CommandException("nolives.command.lives.fail.data");
		return livesData;
	}

	private Pair<UUID, String> getPlayerUuidAndName(MinecraftServer server, ICommandSender sender, String name) throws CommandException {
		UUID uuidToChange;
		String playerName;
		if (name != null) {
			try {
				EntityPlayer player = getPlayer(server, sender, name);
				uuidToChange = player.getUniqueID();
				playerName = player.getName();
			} catch (PlayerNotFoundException e) {
				GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(name);
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

	private int getAmount(ICommandSender sender, String[] args) throws CommandException {
		//Get amount argument
		if (args.length <= 0) throw new WrongUsageException(getUsage(sender));
		return parseInt(args[args.length >= 2 ? 1 : 0]);
	}

	private List<String> getPlayerNameTabCompletions(MinecraftServer server, String[] args) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames()) : Collections.emptyList();
	}

	private class CommandList extends NLCommandBase {
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
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
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

	private class CommandAdd extends NLCommandBase {
		@Override
		public String getName() {
			return "add";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.add.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			PlayerLivesWorldData livesData = getLivesData(sender);
			int amount = getAmount(sender, args);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args[0]);
			int newAmount = livesData.addLives(targetPlayer.getLeft(), amount);
			NoLives.sendMessageText(sender, "lives.add", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}

	private class CommandSub extends NLCommandBase {
		@Override
		public String getName() {
			return "sub";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.sub.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			int amount = getAmount(sender, args);
			PlayerLivesWorldData livesData = getLivesData(sender);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args[0]);
			int newAmount = livesData.subLives(targetPlayer.getLeft(), amount);
			NoLives.sendMessageText(sender, "lives.sub", amount, NoLives.lifeOrLives(amount), targetPlayer.getRight(), newAmount, NoLives.lifeOrLives(newAmount));
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}

	private class CommandSet extends NLCommandBase {
		@Override
		public String getName() {
			return "set";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.set.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			int amount = getAmount(sender, args);
			PlayerLivesWorldData livesData = getLivesData(sender);
			Pair<UUID, String> targetPlayer = getPlayerUuidAndName(server, sender, args[0]);
			int newAmount = livesData.setLives(targetPlayer.getLeft(), amount);
			NoLives.sendMessageText(sender, "lives.set", newAmount, NoLives.lifeOrLives(newAmount), targetPlayer.getRight());
		}

		@Override
		public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
			return getPlayerNameTabCompletions(server, args);
		}
	}

	private class CommandMax extends NLCommandBase {
		@Override
		public String getName() {
			return "max";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.set.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			int amount = getAmount(sender, args);
			boolean configChanged = NLConfig.changeConfig("maxLives", amount,
				config -> amount >= toInt(config.getMinValue()) && amount <= toInt(config.getMaxValue()));
			if (!configChanged)
				throw new CommandException("nolives.command.lives.max.fail", amount);
			NoLives.sendMessageText(sender, "lives.max", amount);
		}

		private int toInt(Object obj) {
			return Integer.parseInt((String) obj);
		}
	}

	private class CommandRevive extends NLCommandBase {
		@Override
		public String getName() {
			return "revive";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.revive.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
			if (!NLConfig.enabled)
				throw new CommandException("nolives.command.lives.disabled");
			Pair<UUID, String> playerPair = getPlayerUuidAndName(server, sender, args.length > 0 ? args[0] : null);
			UUID uuid = playerPair.getLeft();
			PlayerLivesWorldData data = getLivesData(sender);
			PlayerLives playerLives = data.getPlayerLives(uuid);
			int lives = playerLives.lives = NLConfig.defaultLives;

			// Unban player if they're banned
			GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(uuid);
			//noinspection ConstantConditions
			server.getPlayerList().getBannedPlayers().removeEntry(profile);

			EntityPlayer player = server.getPlayerList().getPlayerByUUID(uuid);
			//noinspection ConstantConditions
			if (player != null) {
				player.setGameType(GameType.SURVIVAL);
				NoLives.sendMessageText(player, "revive", lives);
				NoLives.sendMessageText(sender, "revive.success", lives);
			} else {
				data.addPendingRevival(uuid);
				NoLives.sendMessageText(sender, "revive.pending", lives);
			}
		}
	}

	private static class CommandEnable extends NLCommandBase {
		@Override
		public String getName() {
			return "enable";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.enable.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
			if (!NLConfig.enabled)
				NLConfig.changeConfig("enabled", true);
			NoLives.sendMessageText(sender, "lives.enable");
		}
	}

	private static class CommandDisable extends NLCommandBase {
		@Override
		public String getName() {
			return "disable";
		}

		@Override
		public String getUsage(ICommandSender sender) {
			return "nolives.command.lives.disable.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
			if (NLConfig.enabled)
				NLConfig.changeConfig("enabled", false);
			NoLives.sendMessageText(sender, "lives.disable");
		}
	}
}
