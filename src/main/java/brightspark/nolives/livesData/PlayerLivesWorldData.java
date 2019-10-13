package brightspark.nolives.livesData;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class PlayerLivesWorldData extends WorldSavedData {
	private static final String NAME = NoLives.MOD_ID + "_playerLives";
	private Map<UUID, PlayerLives> playerLives = new HashMap<>();
	private List<UUID> pendingRevival = new LinkedList<>();

	public PlayerLivesWorldData() {
		super(NAME);
	}

	public PlayerLivesWorldData(String name) {
		super(name);
	}

	public static PlayerLivesWorldData get(World world) {
		MapStorage storage = world.getMapStorage();
		if (storage == null) return null;
		PlayerLivesWorldData instance = (PlayerLivesWorldData) storage.getOrLoadData(PlayerLivesWorldData.class, NAME);
		if (instance == null) {
			instance = new PlayerLivesWorldData();
			storage.setData(NAME, instance);
		}
		return instance;
	}

	/**
	 * Helper method used by {@link brightspark.nolives.item.ItemHeart} and {@link brightspark.nolives.block.BlockHeart}
	 */
	public static boolean addLives(World world, EntityPlayer player, int amount) {
		PlayerLivesWorldData data = get(world);
		if (data != null) {
			UUID uuid = player.getUniqueID();
			int curLives = data.getLives(uuid);
			if (NLConfig.maxLives > 0 && curLives >= NLConfig.maxLives) {
				NoLives.sendMessageText(player, "max", NLConfig.maxLives);
				return false;
			}
			int newLives = NLConfig.maxLives > 0 ? Math.min(NLConfig.maxLives, curLives + amount) : curLives + amount;
			int lives = data.setLives(uuid, newLives);
			int diff = lives - curLives;
			NoLives.sendMessageText(player, "addLife", diff, NoLives.lifeOrLives(diff), lives, NoLives.lifeOrLives(lives));
			return true;
		}
		NoLives.logger.error("Unable to add life to player " + player.getDisplayNameString() + ". PlayerLivesWorldData was null!");
		return false;
	}

	public PlayerLives getPlayerLives(UUID uuid) {
		return playerLives.computeIfAbsent(uuid, uuid1 -> {
			markDirty();
			return new PlayerLives();
		});
	}

	public int getLives(UUID uuid) {
		return getPlayerLives(uuid).lives;
	}

	public Map<UUID, PlayerLives> getAllLives() {
		return playerLives;
	}

	public int setLives(UUID uuid, int lives) {
		PlayerLives pl = getPlayerLives(uuid);
		pl.lives = Math.max(0, lives);
		markDirty();
		return pl.lives;
	}

	public int addLives(UUID uuid, int amount) {
		PlayerLives pl = getPlayerLives(uuid);
		pl.lives = Math.max(0, pl.lives + amount);
		markDirty();
		return pl.lives;
	}

	public int subLives(UUID uuid, int amount) {
		return addLives(uuid, -amount);
	}

	public void setLastRegenToCurrentTime(EntityPlayer player) {
		PlayerLives pl = getPlayerLives(player.getUniqueID());
		pl.lastRegen = player.world.getTotalWorldTime();
		markDirty();
	}

	public void addPendingRevival(UUID uuid) {
		pendingRevival.add(uuid);
	}

	public boolean getAndRemovePendingRevival(UUID uuid) {
		return pendingRevival.remove(uuid);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList tagList = nbt.getTagList("playerLives", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			UUID uuid = tag.getUniqueId("uuid");
			PlayerLives pl = new PlayerLives(tag.getCompoundTag("pl"));
			playerLives.put(uuid, pl);
		}
		pendingRevival.clear();
		tagList = nbt.getTagList("pendingRevival", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			UUID uuid = tag.getUniqueId("uuid");
			pendingRevival.add(uuid);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList tagList = new NBTTagList();
		for (Map.Entry<UUID, PlayerLives> entry : playerLives.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("uuid", entry.getKey());
			tag.setTag("pl", entry.getValue().serializeNBT());
			tagList.appendTag(tag);
		}
		nbt.setTag("playerLives", tagList);
		tagList = new NBTTagList();
		for (UUID uuid : pendingRevival) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setUniqueId("uuid", uuid);
			tagList.appendTag(tag);
		}
		nbt.setTag("pendingRevival", tagList);
		return nbt;
	}
}
