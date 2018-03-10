package brightspark.nolives;

import net.minecraftforge.common.config.Config;

@Config(modid = NoLives.MOD_ID)
@Config.LangKey(NoLives.MOD_ID + ".title")
public class NLConfig
{
    @Config.Comment("The amount of lives a new player will start with.")
    @Config.RangeInt(min = 1)
    public static int defaultLives = 5;

    @Config.Comment("The amount of lives that will be given when a Heart item is used by a player")
    @Config.RangeInt(min = 1)
    public static int livesFromHeartItem = 1;

    @Config.Comment("The amount of lives that will be given when a Heart block is broken by a player")
    @Config.RangeInt(min = 1)
    public static int livesFromHeartBlock = 1;

    @Config.Comment({"Set to true to drop heart items from the block, or false to just give the player who broke it the lives directly",
        "The amount of items/lives is set by livesFromHeartBlock"})
    public static boolean dropItemsFromBlock = false;

    @Config.Comment("Whether you can use silk touch to pickup the heart block")
    public static boolean canSilkHarvestBlock = true;

    @Config.Comment({
            "If true, on a server the player who ran out of lives will be kicked and banned from the world.",
            "On single player, the player will be kicked and the world will be deleted.",
            "If false, the player will be put into spectator mode."
    })
    public static boolean banOnOutOfLives = true;
}
