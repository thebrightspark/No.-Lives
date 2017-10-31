package brightspark.nolives.capability;

import brightspark.nolives.NoLives;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerLivesProvider implements ICapabilitySerializable<NBTTagInt>
{
    private PlayerLives playerLives;

    public CapabilityPlayerLivesProvider()
    {
        playerLives = new CapabilityPlayerLives();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == NoLives.PLAYER_LIVES;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return hasCapability(capability, facing) ? (T) playerLives : null;
    }

    @Override
    public NBTTagInt serializeNBT()
    {
        return playerLives.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt)
    {
        playerLives.deserializeNBT(nbt);
    }
}
