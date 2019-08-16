package brightspark.nolives.event;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class EventHandler {
	private static boolean deleteWorld = false;

	public static boolean shouldDeleteWorld() {
		if (deleteWorld) {
			deleteWorld = false;
			return true;
		} else
			return false;
	}

	private static boolean isHardcore(World world) {
		return world.getWorldInfo().isHardcoreModeEnabled();
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlayerDeathSubLives(LivingDeathEvent event) {
		if (!(event.getEntityLiving() instanceof EntityPlayerMP) || isHardcore(event.getEntityLiving().world)) return;
		EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
		LifeChangeEvent.LifeLossEvent lifeLossEvent = new LifeChangeEvent.LifeLossEvent(player, 1);
		if (!MinecraftForge.EVENT_BUS.post(lifeLossEvent)) {
			int livesToLose = lifeLossEvent.getLivesToLose();
			if (livesToLose > 0) {
				PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
				if (data == null) return;
				data.setLastRegenToCurrentTime(player);
				int livesLeft = data.subLives(player.getUniqueID(), livesToLose);
				String message = NoLives.getRandomDeathMessage();
				if (message != null) player.sendMessage(new TextComponentString(String.format(message, livesLeft)));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerDeathKick(LivingDeathEvent event) {
		//Kick and ban the player if lives are 0 after a short delay (to allow for dropping of items and other things)
		if (!(event.getEntityLiving() instanceof EntityPlayer) || isHardcore(event.getEntityLiving().world))
			return;
		EntityPlayer player = (EntityPlayer) event.getEntityLiving();
		if (PlayerLivesWorldData.get(player.world).getLives(player.getUniqueID()) > 0)
			return;
		MinecraftServer server = player.getServer();
		if (server == null)
			return;

		//Message all players
		String message = NoLives.getRandomOutOfLivesMessage();
		if (message != null)
			server.getPlayerList().getPlayers().forEach((p) -> p.sendMessage(new TextComponentString(String.format(message, player.getDisplayNameString()))));
		//Play death sound
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1f, 0.5f);

		if (!NLConfig.banOnOutOfLives)
			player.setGameType(GameType.SPECTATOR);
		else if (server.isDedicatedServer()) {
			//Multiplayer server - kick player
			kickBanPlayer((EntityPlayerMP) player, server);
		} else {
			if (player.getName().equals(server.getServerOwner())) {
				if (server.isSinglePlayer()) {
					//Single player - delete world
					deleteWorld = true;
					server.initiateShutdown();
				} else {
					//LAN server host. Can't kick/ban them! Must put them into spectator mode.
					player.setGameType(GameType.SPECTATOR);
				}
			} else if (player instanceof EntityPlayerMP) {
				//Other player - kick player
				kickBanPlayer((EntityPlayerMP) player, server);
			}
		}
	}

	private static void kickBanPlayer(EntityPlayerMP player, MinecraftServer server) {
		UserListBansEntry banEntry = new UserListBansEntry(player.getGameProfile(), null, NoLives.MOD_NAME, null, "You ran out of lives!");
		server.getPlayerList().getBannedPlayers().addEntry(banEntry);
		player.connection.disconnect(new TextComponentTranslation(NoLives.MOD_ID + ".message.kick"));
	}

	@SubscribeEvent()
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (NLConfig.regenSeconds > 0 && event.phase == TickEvent.Phase.END) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server == null || server.isHardcore()) return;
			WorldServer overworld = server.getWorld(0);
			long worldTime = overworld.getTotalWorldTime();
			//Only check once every second
			if (worldTime % 5 == 0) {
				PlayerLivesWorldData data = PlayerLivesWorldData.get(overworld);
				if (data == null) return;
				long lastRegenTime = worldTime - (NLConfig.regenSeconds * 20);
				//For each player, give them a life if it's been long enough since their last
				server.getPlayerList().getPlayers().forEach(player -> {
					if (!player.isDead) {
						PlayerLives pl = data.getPlayerLives(player.getUniqueID());
						if (pl.lives > 0 && pl.lives < NLConfig.regenMaxLives && pl.lastRegen <= lastRegenTime) {
							pl.lastRegen = worldTime;
							LifeChangeEvent.LifeGainEvent lifeGainEvent = new LifeChangeEvent.LifeGainEvent(player, 1, LifeChangeEvent.LifeGainEvent.GainType.REGEN);
							if (!MinecraftForge.EVENT_BUS.post(lifeGainEvent) && lifeGainEvent.getLivesToGain() > 0) {
								int gained = lifeGainEvent.getLivesToGain();
								pl.lives += gained;
								NoLives.sendMessageText(player, "regen", gained, NoLives.lifeOrLives(gained), pl.lives, NoLives.lifeOrLives(pl.lives));
							}
							data.markDirty();
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			//Just need to reset the last regen time so they don't start regenerating lives as soon as they respawn
			EntityPlayer player = event.getEntityPlayer();
			PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
			if (data != null)
				data.setLastRegenToCurrentTime(player);
		}
	}
}
