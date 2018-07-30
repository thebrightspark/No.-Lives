package brightspark.nolives.livesData;

import brightspark.nolives.NLConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerLives implements INBTSerializable<NBTTagCompound>
{
    public int lives = NLConfig.defaultLives;
    public long lastRegen = 0;

    public PlayerLives() {}

    public PlayerLives(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("lives", lives);
        nbt.setLong("lastRegen", lastRegen);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        lives = nbt.getInteger("lives");
        lastRegen = nbt.getLong("lastRegen");
    }
}
