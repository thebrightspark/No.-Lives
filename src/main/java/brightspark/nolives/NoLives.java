package brightspark.nolives;

import brightspark.nolives.block.BlockHeart;
import brightspark.nolives.command.CommandLives;
import brightspark.nolives.event.EventHandler;
import brightspark.nolives.item.ItemHeart;
import brightspark.nolives.livesData.MessageGetLives;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mod(modid = NoLives.MOD_ID, name = NoLives.MOD_NAME, version = NoLives.VERSION)
public class NoLives
{
    public static final String MOD_ID = "nolives";
    public static final String MOD_NAME = "No. Lives";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static NoLives INSTANCE;

    public static Logger logger;
    public static SimpleNetworkWrapper NETWORK;
    public static CreativeTabs tab = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(itemHeart);
        }
    };

    public static Item itemHeart = new ItemHeart();
    public static Block blockHeart = new BlockHeart();

    private static Random rand = new Random();
    private static List<String> deathMessages, outOfLivesMessages;

    private String worldName = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        NETWORK.registerMessage(MessageGetLives.Handler.class, MessageGetLives.class, 0, Side.CLIENT);

        File configDir = new File(event.getModConfigurationDirectory(), MOD_ID);
        if(!configDir.mkdirs())
            logger.error("Config directory either already exists or couldn't be created");
        deathMessages = readTextFile(new File(configDir, "deathMessages.txt"), "You have %s lives left");
        outOfLivesMessages = readTextFile(new File(configDir, "outOfLivesMessages.txt"), "%s has run out of lives!");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        if(event.getServer().isSinglePlayer())
            worldName = event.getServer().getWorldName();

        event.registerServerCommand(new CommandLives());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
        if(EventHandler.shouldDeleteWorld() && worldName != null)
            Minecraft.getMinecraft().getSaveLoader().deleteWorldDirectory(worldName);
    }

    private static List<String> readTextFile(File textFile, String defaultText)
    {
        try
        {
            if(!textFile.exists())
            {
                Files.write(textFile.toPath(), Collections.singleton(defaultText), StandardOpenOption.CREATE_NEW);
                logger.info("Created default " + textFile.getName());
            }
            return Files.readAllLines(textFile.toPath());
        }
        catch(IOException e)
        {
            logger.error("Error handling text file '" + textFile.getName() + "'", e);
        }
        return null;
    }

    private static String randValue(List<String> list)
    {
        return list.get(rand.nextInt(list.size()));
    }

    public static String getRandomDeathMessage()
    {
        return deathMessages.isEmpty() ? null : randValue(deathMessages);
    }

    public static String getRandomOutOfLivesMessage()
    {
        return outOfLivesMessages.isEmpty() ? null : randValue(outOfLivesMessages);
    }
}
