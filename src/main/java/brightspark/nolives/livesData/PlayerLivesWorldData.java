package brightspark.nolives.livesData;

import brightspark.nolives.NoLives;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
    private Map<UUID, Integer> playerLives = new HashMap<>();

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

    public int getLives(UUID uuid)
    {
        Integer lives = playerLives.putIfAbsent(uuid, 0);
        return lives == null ? 0 : lives;
    }

    public Map<UUID, Integer> getAllLives()
    {
        return playerLives;
    }

    public int setLives(UUID uuid, int lives)
    {
        int newLives = Math.max(0, lives);
        playerLives.put(uuid, newLives);
        return newLives;
    }

    public int addLives(UUID uuid, int amount)
    {
        int current = getLives(uuid);
        return setLives(uuid, current + amount);
    }

    public int subLives(UUID uuid, int amount)
    {
        int current = getLives(uuid);
        return setLives(uuid, current - amount);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("playerLives", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            UUID uuid = tag.getUniqueId("uuid");
            int lives = tag.getInteger("lives");
            playerLives.put(uuid, lives);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = new NBTTagList();
        for(Map.Entry<UUID, Integer> entry : playerLives.entrySet())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("uuid", entry.getKey());
            tag.setInteger("lives", entry.getValue());
            tagList.appendTag(tag);
        }
        nbt.setTag("playerLives", tagList);
        return nbt;
    }
}
