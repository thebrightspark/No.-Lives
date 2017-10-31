package brightspark.nolives;

import brightspark.nolives.capability.CapabilityPlayerLives;
import brightspark.nolives.capability.PlayerLives;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

@Mod(modid = NoLives.MOD_ID, name = NoLives.MOD_NAME, version = NoLives.VERSION)
public class NoLives
{
    public static final String MOD_ID = "nolives";
    public static final String MOD_NAME = "No. Lives";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(MOD_ID)
    public static NoLives INSTANCE;

    public static SimpleNetworkWrapper NETWORK;

    @CapabilityInject(PlayerLives.class)
    public static Capability<PlayerLives> PLAYER_LIVES = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        //TODO: Add message

        CapabilityManager.INSTANCE.register(PlayerLives.class, new Capability.IStorage<PlayerLives>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<PlayerLives> capability, PlayerLives instance, EnumFacing side)
            {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<PlayerLives> capability, PlayerLives instance, EnumFacing side, NBTBase nbt)
            {
                instance.deserializeNBT((NBTTagInt) nbt);
            }
        }, CapabilityPlayerLives::new);
    }

    public static PlayerLives getLivesCap(EntityPlayer player)
    {
        return player.getCapability(PLAYER_LIVES, null);
    }

    public static boolean hasLivesCap(EntityPlayer player)
    {
        return player.hasCapability(PLAYER_LIVES, null);
    }
}
