package brightspark.nolives.capability;

import brightspark.nolives.Config;
import brightspark.nolives.NoLives;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;

public class CapabilityPlayerLives implements PlayerLives
{
    public static final ResourceLocation playerLivesRL = new ResourceLocation(NoLives.MOD_ID, "PlayerLives");
    private int lives;

    public CapabilityPlayerLives()
    {
        lives = Config.defaultLives;
    }

    @Override
    public int getLives()
    {
        return lives;
    }

    @Override
    public int setLives(int amount)
    {
        lives = Math.max(0, amount);
        return lives;
    }

    @Override
    public int addLives(int amount)
    {
        return setLives(lives + amount);
    }

    @Override
    public int subLives(int amount)
    {
        return setLives(lives - amount);
    }

    @Override
    public NBTTagInt serializeNBT()
    {
        return new NBTTagInt(lives);
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt)
    {
        lives = nbt.getInt();
    }
}
