package brightspark.nolives;

import brightspark.nolives.capability.CapabilityPlayerLives;
import brightspark.nolives.capability.CapabilityPlayerLivesProvider;
import brightspark.nolives.capability.PlayerLives;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class EventHandler
{
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, NoLives.MOD_NAME + " Kick/Ban"));

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        //Attach the capability to players
        Entity entity = event.getObject();
        if(!(entity instanceof EntityPlayer) || entity.hasCapability(NoLives.PLAYER_LIVES, null))
            return;
        event.addCapability(CapabilityPlayerLives.playerLivesRL, new CapabilityPlayerLivesProvider());
    }

    @SubscribeEvent
    public static void onClonePlayer(net.minecraftforge.event.entity.player.PlayerEvent.Clone event)
    {
        //Copy the capability to the new player entity on death
        if(!event.isWasDeath() || !(event.getEntityPlayer() instanceof EntityPlayerMP))
            return;
        PlayerLives oldCap = NoLives.getLivesCap(event.getOriginal());
        PlayerLives newCap = NoLives.getLivesCap(event.getEntityPlayer());
        if(oldCap == null || newCap == null)
            return;
        newCap.deserializeNBT(oldCap.serializeNBT());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeathSubLives(LivingDeathEvent event)
    {
        //Reduce player lives on death and fire LifeLossEvent
        if(!(event.getEntityLiving() instanceof EntityPlayerMP))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if(!NoLives.hasLivesCap(player) || MinecraftForge.EVENT_BUS.post(new LifeLossEvent(player)))
            return;
        int livesLeft = NoLives.getLivesCap(player).subLives(1);
        player.sendMessage(new TextComponentString("You have " + livesLeft + " lives left"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathKick(LivingDeathEvent event)
    {
        //Kick and ban the player if lives are 0 after a short delay (to allow for dropping of items and other things)
        if(!(event.getEntityLiving() instanceof EntityPlayerMP))
            return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        PlayerLives lives = NoLives.getLivesCap(player);
        if(lives == null || lives.getLives() > 0)
            return;
        player.getServer().sendMessage(player.getDisplayName().appendText(" has run out of lives!"));
        scheduledExecutorService.schedule(() ->
        {
            MinecraftServer server = player.getServer();
            UserListBansEntry banEntry = new UserListBansEntry(player.getGameProfile(), null, NoLives.MOD_NAME, null, "You ran out of lives!");
            server.getPlayerList().getBannedPlayers().addEntry(banEntry);
            player.connection.disconnect(new TextComponentString("You ran out of lives!"));
        }, 1, TimeUnit.SECONDS);
    }
}
