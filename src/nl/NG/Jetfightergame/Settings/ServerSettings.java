package nl.NG.Jetfightergame.Settings;

import java.util.Random;

/**
 * a class that harbours the variables that may or may not be changed by the player
 * @see MenuStyleSettings
 * @see KeyBindings
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
@SuppressWarnings("ConstantConditions")
public final class ServerSettings {
    public static final boolean DEBUG = true;

    /** general settings */
    public static final String GAME_NAME = "Jet Fighter Game"; // laaaame
    public static final short TARGET_TPS = 10;
    public static final int COLLISION_DETECTION_LEVEL = 1;

    /** connection settings */
    public static final int SERVER_PORT = 3039;

    /** collision settings */
    public static final short COLLISION_RESPONSE_LEVEL = 0;
    public static final boolean DO_COLLISION_RESPONSE = COLLISION_RESPONSE_LEVEL >= 0;
    public static final int MAX_COLLISION_ITERATIONS = 250/TARGET_TPS;
    // force factor of how strong two colliding planes bump off
    public static final float BUMP_POWER = 5;

    /** miscellaneous */
    // universal random to be used everywhere
    public static final Random random = new Random();
    public static final int INTERPOLATION_QUEUE_SIZE = 10;
    public static boolean RENDER_ENABLED = false;

    public static int entityIDNumber = 0;
}
