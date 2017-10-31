package brightspark.nolives;

import brightspark.nolives.capability.PlayerLives;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LifeLossEvent extends Event
{
    private EntityPlayer player;
    private PlayerLives lives;

    public LifeLossEvent(EntityPlayer player)
    {
        this.player = player;
        lives = NoLives.getLivesCap(player);
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }

    public PlayerLives getLives()
    {
        return lives;
    }
}
