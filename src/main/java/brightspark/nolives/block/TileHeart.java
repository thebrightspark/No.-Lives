package brightspark.nolives.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;

public class TileHeart extends TileEntity implements ITickable
{
    private long lastTicks = 0;
    private long ticks = 0;
    private double lastRotation = 0;
    private double rotation = 0;

    @Override
    public void update()
    {
        ticks++;
    }

    public void updateRotation(Vec3d playerPos)
    {
        if(ticks != lastTicks)
        {
            lastTicks = ticks;
            lastRotation = rotation;
            double distance = playerPos.distanceTo(new Vec3d(pos));
            double speed = Math.max(1d, 75d / distance);
            rotation += speed;
        }
    }

    public double getRotation(double partialTicks)
    {
        return lastRotation + ((rotation - lastRotation) * partialTicks);
    }
}
