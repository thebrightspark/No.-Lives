package brightspark.nolives.capability;

import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.util.INBTSerializable;

public interface PlayerLives extends INBTSerializable<NBTTagInt>
{
    int getLives();

    int setLives(int amount);

    int addLives(int amount);

    int subLives(int amount);
}
