package brightspark.nolives.livesData;

import brightspark.nolives.NoLives;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLivesWorldData extends WorldSavedData
{
    private static final String NAME = NoLives.MOD_ID + "_playerLives";
    private Map<UUID, PlayerLives> playerLives = new HashMap<>();

    public PlayerLivesWorldData()
    {
        super(NAME);
    }

    public PlayerLivesWorldData(String name)
    {
        super(name);
    }

    public static PlayerLivesWorldData get(World world)
    {
        MapStorage storage = world.getMapStorage();
        if(storage == null) return null;
        PlayerLivesWorldData instance = (PlayerLivesWorldData) storage.getOrLoadData(PlayerLivesWorldData.class, NAME);
        if(instance == null)
        {
            instance = new PlayerLivesWorldData();
            storage.setData(NAME, instance);
        }
        return instance;
    }

    public static boolean addLives(World world, EntityPlayer player, int amount)
    {
        PlayerLivesWorldData data = get(world);
        if(data != null)
        {
            data.addLives(player.getUniqueID(), amount);
            player.sendMessage(new TextComponentTranslation(NoLives.MOD_ID + ".addLife", data.getLives(player.getUniqueID())));
            return true;
        }
        NoLives.logger.error("Unable to add life to player " + player.getDisplayNameString() + ". PlayerLivesWorldData was null!");
        return false;
    }

    public PlayerLives getPlayerLives(UUID uuid)
    {
        PlayerLives pl = playerLives.get(uuid);
        if(pl == null)
        {
            pl = new PlayerLives();
            playerLives.put(uuid, pl);
        }
        return pl;
    }

    public int getLives(UUID uuid)
    {
        return getPlayerLives(uuid).lives;
    }

    public Map<UUID, PlayerLives> getAllLives()
    {
        return playerLives;
    }

    public int setLives(UUID uuid, int lives)
    {
        PlayerLives pl = getPlayerLives(uuid);
        pl.lives = Math.max(0, lives);
        return pl.lives;
    }

    public int addLives(UUID uuid, int amount)
    {
        PlayerLives pl = getPlayerLives(uuid);
        pl.lives = Math.max(0, pl.lives + amount);
        return pl.lives;
    }

    public int subLives(UUID uuid, int amount)
    {
        return addLives(uuid, -amount);
    }

    public void setLastRegenToCurrentTime(EntityPlayer player)
    {
        PlayerLives pl = getPlayerLives(player.getUniqueID());
        pl.lastRegen = player.world.getTotalWorldTime();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("playerLives", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            UUID uuid = tag.getUniqueId("uuid");
            PlayerLives pl = new PlayerLives(tag.getCompoundTag("pl"));
            playerLives.put(uuid, pl);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = new NBTTagList();
        for(Map.Entry<UUID, PlayerLives> entry : playerLives.entrySet())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("uuid", entry.getKey());
            tag.setTag("pl", entry.getValue().serializeNBT());
            tagList.appendTag(tag);
        }
        nbt.setTag("playerLives", tagList);
        return nbt;
    }
}
