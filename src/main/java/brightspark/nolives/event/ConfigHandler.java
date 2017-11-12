package brightspark.nolives.event;

import brightspark.nolives.Config;
import brightspark.nolives.NoLives;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class ConfigHandler
{
    public static final String GENERAL = Configuration.CATEGORY_GENERAL;

    private static Configuration config;

    public static void init(File configFile)
    {
        if(config == null)
        {
            config = new Configuration(configFile);
            loadConfig();
        }
    }

    private static void loadConfig()
    {
        Config.defaultLives = config.getInt("defaultLives", GENERAL, Config.defaultLives, 1, Integer.MAX_VALUE, "The amount of lives a new player will start with");
        Config.banOnOutOfLives = config.getBoolean("banOnOutOfLives", GENERAL, Config.banOnOutOfLives,
            "If true, on a server the player who ran out of lives will be kicked and banned from the world.\n" +
                    "On single player, the player will be kicked and the world will be deleted.\n" +
                    "If false, the player will be put into spectator mode.");

        if(config.hasChanged())
            config.save();
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equals(NoLives.MOD_ID))
            loadConfig();
    }
}
