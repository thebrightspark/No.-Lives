package brightspark.nolives.event;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This is the parent class for the {@link LifeLossEvent} and {@link LifeGainEvent}
 * These events are only fired on the server side
 */
@Cancelable
public class LifeChangeEvent extends Event
{
    private final EntityPlayerMP player;
    private final int currentLives;
    int amount;

    LifeChangeEvent(EntityPlayerMP player, int amount)
    {
        this.player = player;
        PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
        currentLives = data.getLives(player.getUniqueID());
        this.amount = amount;
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
     * Fired when a player is about to gain a life
     */
    public static class LifeGainEvent extends LifeChangeEvent
    {
        public enum GainType
        {
            ITEM,
            BLOCK,
            REGEN
        }

        private final GainType type;

        public LifeGainEvent(EntityPlayerMP player, int amount, GainType type)
        {
            super(player, amount);
            this.type = type;
        }

        /**
         * Gets the number of lives the player will gain
         */
        public int getLivesToGain()
        {
            return amount;
        }

        /**
         * Sets the number of lives the player will gain
         */
        public int setLivesToGain(int amount)
        {
            return this.amount = amount;
        }

        /**
         * Gets the type of life gain this is
         * ITEM  -> Life gained by right clicking a Heart item
         * BLOCK -> Life gained by breaking a Heart block
         * REGEN -> Life gained from regeneration
         */
        public GainType getGainType()
        {
            return type;
        }
    }

    /**
     * Fired when a player dies and is about to lose a life
     */
    public static class LifeLossEvent extends LifeChangeEvent
    {
        public LifeLossEvent(EntityPlayerMP player, int amount)
        {
            super(player, amount);
        }

        /**
         * Gets the number of lives the player will lose
         */
        public int getLivesToLose()
        {
            return amount;
        }

        /**
         * Sets the number of lives the player will lose
         */
        public int setLivesToLose(int amount)
        {
            return this.amount = amount;
        }
    }
}
