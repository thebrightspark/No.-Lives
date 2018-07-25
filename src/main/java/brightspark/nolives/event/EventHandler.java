package brightspark.nolives.event;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler
{
    private static boolean deleteWorld = false;

    public static boolean shouldDeleteWorld()
    {
        if(deleteWorld)
        {
            deleteWorld = false;
            return true;
        }
        else
            return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeathSubLives(LivingDeathEvent event)
    {
        if(!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        LifeLossEvent lifeLossEvent = new LifeLossEvent(player);
        if(!MinecraftForge.EVENT_BUS.post(lifeLossEvent))
        {
            int livesToLose = lifeLossEvent.getLivesToLose();
            if(livesToLose > 0)
            {
                PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
                int livesLeft = data.subLives(player.getUniqueID(), livesToLose);
                String message = NoLives.getRandomDeathMessage();
                if(message != null) player.sendMessage(new TextComponentString(String.format(message, livesLeft)));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathKick(LivingDeathEvent event)
    {
        //Kick and ban the player if lives are 0 after a short delay (to allow for dropping of items and other things)
        if(!(event.getEntityLiving() instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if(PlayerLivesWorldData.get(player.world).getLives(player.getUniqueID()) > 0)
            return;
        MinecraftServer server = player.getServer();
        if(server == null)
            return;

        //Message all players
        String message = NoLives.getRandomOutOfLivesMessage();
        if(message != null)
            server.getPlayerList().getPlayers().forEach((p) -> p.sendMessage(new TextComponentString(String.format(message, player.getDisplayNameString()))));
        //Play death sound
        player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1f, 0.5f);

        if(!NLConfig.banOnOutOfLives)
            player.setGameType(GameType.SPECTATOR);
        else if(server.isDedicatedServer())
        {
            //Multiplayer server - kick player
            kickBanPlayer((EntityPlayerMP) player, server);
        }
        else
        {
            if(player.getName().equals(server.getServerOwner()))
            {
                if(server.isSinglePlayer())
                {
                    //Single player - delete world
                    deleteWorld = true;
                    server.initiateShutdown();
                }
                else
                {
                    //LAN server host. Can't kick/ban them! Must put them into spectator mode.
                    player.setGameType(GameType.SPECTATOR);
                }
            }
            else if(player instanceof EntityPlayerMP)
            {
                //Other player - kick player
                kickBanPlayer((EntityPlayerMP) player, server);
            }
        }
    }

    private static void kickBanPlayer(EntityPlayerMP player, MinecraftServer server)
    {
        UserListBansEntry banEntry = new UserListBansEntry(player.getGameProfile(), null, NoLives.MOD_NAME, null, "You ran out of lives!");
        server.getPlayerList().getBannedPlayers().addEntry(banEntry);
        player.connection.disconnect(new TextComponentString("You ran out of lives!"));
    }
}
