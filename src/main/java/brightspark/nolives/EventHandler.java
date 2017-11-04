package brightspark.nolives;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler
{
    private static PlayerLivesWorldData playerLives;

    private static PlayerLivesWorldData getPlayerLives(World world)
    {
        if(playerLives == null)
            playerLives = PlayerLivesWorldData.get(world);
        return playerLives;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeathSubLives(LivingDeathEvent event)
    {
        //Reduce player lives on death and fire LifeLossEvent
        if(!(event.getEntityLiving() instanceof EntityPlayerMP))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if(MinecraftForge.EVENT_BUS.post(new LifeLossEvent(player)))
            return;
        int livesLeft = getPlayerLives(player.world).subLives(player.getUniqueID(), 1);
        player.sendMessage(new TextComponentString("You have " + livesLeft + " lives left"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathKick(LivingDeathEvent event)
    {
        //Kick and ban the player if lives are 0 after a short delay (to allow for dropping of items and other things)
        if(!(event.getEntityLiving() instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if(getPlayerLives(player.world).getLives(player.getUniqueID()) > 0)
            return;
        MinecraftServer server = player.getServer();
        server.getPlayerList().getPlayers().forEach((p) -> p.sendMessage(player.getDisplayName().appendText(" has run out of lives!")));

        //Play death sound
        if(!server.isSinglePlayer() || !Config.banOnOutOfLives)
            player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1f, 0.5f);

        if(!Config.banOnOutOfLives)
            player.setGameType(GameType.SPECTATOR);
        else
        {
            if(server.isSinglePlayer() && player.getName().equals(server.getServerOwner()))
            {
                //Single player world
                if(player instanceof EntityPlayerMP)
                    //TODO: This just crashes the client?
                    ((EntityPlayerMP) player).connection.disconnect(new TextComponentString("You ran out of lives!"));
                else
                {
                    //TODO: Delete world
                }
            }
            else
            {
                //Multiplayer server world
                if(player instanceof EntityPlayerMP)
                {
                    UserListBansEntry banEntry = new UserListBansEntry(player.getGameProfile(), null, NoLives.MOD_NAME, null, "You ran out of lives!");
                    server.getPlayerList().getBannedPlayers().addEntry(banEntry);
                    ((EntityPlayerMP) player).connection.disconnect(new TextComponentString("You ran out of lives!"));
                }
            }
        }
    }
}
