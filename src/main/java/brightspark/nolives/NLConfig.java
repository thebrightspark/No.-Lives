package brightspark.nolives;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

@Config(modid = NoLives.MOD_ID)
public class NLConfig {
	@Config.Comment({
		"Whether this mod is enabled",
		"Disabling will completely disable everything in this mod, but the item and block will still exist"
	})
	public static boolean enabled = true;

	@Config.Comment("The amount of lives a new player will start with")
	@Config.RangeInt(min = 1)
	public static int defaultLives = 5;

	@Config.Comment({
		"The maximum number of lives a player can have - only limits gaining through Heart items or blocks (no limit for commands)",
		"This value can be smaller than the defaultLives config",
		"A value of 0 will mean there is no limit"
	})
	@Config.RangeInt(min = 0)
	public static int maxLives = 0;

	@Config.Comment("The amount of lives that will be given when a Heart item is used by a player")
	@Config.RangeInt(min = 1)
	public static int livesFromHeartItem = 1;

	@Config.Comment("The amount of lives that will be given when a Heart block is broken by a player")
	@Config.RangeInt(min = 1)
	public static int livesFromHeartBlock = 1;

	@Config.Comment({
		"Set to true to drop Heart items from the block, or false to just give the player who broke it the lives directly",
		"The amount of items/lives is set by livesFromHeartBlock"})
	public static boolean dropItemsFromBlock = false;

	@Config.Comment("Whether you can use silk touch to pickup the Heart block")
	public static boolean canSilkHarvestBlock = true;

	@Config.Comment({
		"If true, on a server the player who ran out of lives will be kicked and banned from the world",
		"On single player, the player will be kicked and the world will be deleted",
		"If false, the player will be put into spectator mode"
	})
	public static boolean banOnOutOfLives = true;

	@Config.Comment({
			"If true, then if a player had previously run out of lives and is currently in Spectator mode and they are later given another life in some way, then on login they will be set back to Survival mode",
			"If false, then their game mode will not be changed and may need an OP to manually change their game mode if desired",
            "Note that this config will not work if the player is banned - you'll need to use the revive command or manually unban the player"
	})
	public static boolean reviveOnLogin = false;

	@Config.Comment({
		"The frequency at which players will regenerate lives (in seconds), up until the regenMaxLives config",
		"If set to 0, then players will not regen lives at all"
	})
	@Config.RangeInt(min = 0)
	public static int regenSeconds = 0;

	@Config.Comment("The max amount of lives a player can gain from the regeneration (see the regenSeconds config)")
	@Config.RangeInt(min = 1)
	public static int regenMaxLives = 5;

	@Mod.EventBusSubscriber
	public static class ConfigHandler {
		@SubscribeEvent
		public static void configChanged(ConfigChangedEvent event) {
			//Make sure configs are updated when they're changed in-game
			if (event.getModID().equals(NoLives.MOD_ID))
				ConfigManager.sync(NoLives.MOD_ID, Config.Type.INSTANCE);
		}
	}

	public static void changeConfig(String configPath, Object newValue) {
		changeConfig(configPath, newValue, config -> true);
	}

	public static boolean changeConfig(String configPath, Object newValue, Predicate<IConfigElement> validator) {
		configPath = NoLives.MOD_ID + "." + configPath;
		IConfigElement config = getConfig(ConfigElement.from(NLConfig.class), configPath.split("\\."), 0);
		if (config == null)
			throw new RuntimeException(String.format("No config found for path %s!", configPath));
		if (!validator.test(config))
			return false;
		config.set(newValue);
		// We presume a world is running, since this is only called from a command for now
		ConfigChangedEvent event = new ConfigChangedEvent.OnConfigChangedEvent(NoLives.MOD_ID, configPath, true, config.requiresMcRestart());
		MinecraftForge.EVENT_BUS.post(event);
		if (!event.getResult().equals(Event.Result.DENY))
			MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(NoLives.MOD_ID, configPath, true, config.requiresMcRestart()));
		return true;
	}

	//Recursive method to find the config
	private static IConfigElement getConfig(IConfigElement element, String[] path, int level) {
		String name = path[level];
		if (element.getName().equalsIgnoreCase(name)) {
			if (element.isProperty())
				return element;
			else //noinspection ConstantConditions
				if (level < path.length) {
					int nextLevel = level + 1;
					for (IConfigElement e : element.getChildElements()) {
						IConfigElement result = getConfig(e, path, nextLevel);
						if (result != null) return result;
					}
				}
		}
		return null;
	}
}
