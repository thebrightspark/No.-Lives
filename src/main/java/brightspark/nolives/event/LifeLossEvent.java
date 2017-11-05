package brightspark.nolives.event;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LifeLossEvent extends Event
{
    private PlayerLivesWorldData data;
    private EntityPlayer player;
    private int lives;

    public LifeLossEvent(EntityPlayer player)
    {
        data = PlayerLivesWorldData.get(player.world);
        this.player = player;
        lives = data.getLives(player.getUniqueID());
    }

    public EntityPlayer getPlayer()
    {
        return player;
    }

    public int getLives()
    {
        return lives;
    }

    public void setLives(int amount)
    {
        data.setLives(player.getUniqueID(), amount);
    }
}
