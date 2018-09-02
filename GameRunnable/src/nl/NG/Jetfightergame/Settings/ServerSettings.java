package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetBasic;


/**
 * a class that harbours the variables that may or may not be changed by the player
 * @see MenuStyleSettings
 * @see KeyBinding
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
@SuppressWarnings("ConstantConditions")
public final class ServerSettings {
    public static boolean DEBUG = false;
    public static boolean SERVER_MAKE_REPLAY = true;

    /** general settings */
    public static final String GAME_NAME = "Jet Fighter Game"; // laaaame
    public static int TARGET_TPS = 30;

    /** connection settings */
    public static int SERVER_PORT = 3039;

    /** collision detection */
    public static final int MAX_COLLISION_ITERATIONS = 100 / TARGET_TPS;
    public static final float BUMPOFF_SPEED = 15f;
    public static final float BUMPOFF_ENERGY = (0.5f * JetBasic.MASS * BUMPOFF_SPEED * BUMPOFF_SPEED); // e = 0.5*m*v*v in joule

    /** miscellaneous */
    public static final float COUNT_DOWN = 3f;
    public static final int INTERPOLATION_QUEUE_SIZE = 120 / TARGET_TPS + 10;
    public static final float POWERUP_COLLECTION_RANGE = 18f;
    public static final float GENERAL_SPEED_FACTOR = 2.0f;

    public static final float PRINT_STATE_INTERVAL = 0;
    public static int NOF_FUN = 3;

    public static final int CUBE_SIZE_LARGE = 25;
    public static final float CUBE_MASS_LARGE = CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * 0.1f;
}
