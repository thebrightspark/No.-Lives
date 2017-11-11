package brightspark.nolives;

public class Config
{
    /**
     * The amount of lives a new player will start with.
     */
    public static int defaultLives = 5;

    /**
     * If true, on a server the player who ran out of lives will be kicked and banned from the world.
     * On single player, the player will be kicked and the world will be deleted.
     *
     * If false, the player will be put into spectator mode.
     */
    public static boolean banOnOutOfLives = true;
}
