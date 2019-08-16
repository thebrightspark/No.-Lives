package brightspark.nolives;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = NoLives.MOD_ID)
@Config.LangKey(NoLives.MOD_ID + ".title")
public class NLConfig {
	@Config.Comment("The amount of lives a new player will start with")
	@Config.RangeInt(min = 1)
	public static int defaultLives = 5;

	@Config.Comment("The amount of lives that will be given when a Heart item is used by a player")
	@Config.RangeInt(min = 1)
	public static int livesFromHeartItem = 1;

	@Config.Comment("The amount of lives that will be given when a Heart block is broken by a player")
	@Config.RangeInt(min = 1)
	public static int livesFromHeartBlock = 1;

	@Config.Comment({
		"Set to true to drop heart items from the block, or false to just give the player who broke it the lives directly",
		"The amount of items/lives is set by livesFromHeartBlock"})
	public static boolean dropItemsFromBlock = false;

	@Config.Comment("Whether you can use silk touch to pickup the heart block")
	public static boolean canSilkHarvestBlock = true;

	@Config.Comment({
		"If true, on a server the player who ran out of lives will be kicked and banned from the world",
		"On single player, the player will be kicked and the world will be deleted",
		"If false, the player will be put into spectator mode"
	})
	public static boolean banOnOutOfLives = true;

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
}
