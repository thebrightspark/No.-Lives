package brightspark.nolives.event;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired when a player dies
 */
@Cancelable
public class LifeLossEvent extends Event
{
    private final EntityPlayerMP player;
    private final int currentLives;
    private int livesToLose;

    public LifeLossEvent(EntityPlayerMP player)
    {
        this.player = player;
        PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
        currentLives = data.getLives(player.getUniqueID());
        livesToLose = 1;
    }

    /**
     * Gets the player that has just died
     */
    public EntityPlayerMP getPlayer()
    {
        return player;
    }

    /**
     * Gets the amount of lives the player has before they died
     */
    public int getCurrentLives()
    {
        return currentLives;
    }

    /**
     * Gets the number of lives the player will lose
     */
    public int getLivesToLose()
    {
        return livesToLose;
    }

    /**
     * Sets the number of lives the player will lose
     */
    public int setLivesToLose(int amount)
    {
        return livesToLose = amount;
    }
}
