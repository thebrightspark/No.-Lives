package brightspark.nolives;

import brightspark.nolives.command.CommandLives;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = NoLives.MOD_ID, name = NoLives.MOD_NAME, version = NoLives.VERSION)
public class NoLives
{
    public static final String MOD_ID = "nolives";
    public static final String MOD_NAME = "No. Lives";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static NoLives INSTANCE;

    public static SimpleNetworkWrapper NETWORK;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        NETWORK.registerMessage(MessageGetLives.Handler.class, MessageGetLives.class, 0, Side.CLIENT);

        //TODO: Use text files for custom messages
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLives());
    }
}
