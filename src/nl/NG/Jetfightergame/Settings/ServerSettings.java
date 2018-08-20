package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetBasic;

/**
 * a class that harbours the variables that may or may not be changed by the player
 * @see MenuStyleSettings
 * @see KeyBindings
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
@SuppressWarnings("ConstantConditions")
public final class ServerSettings {
    public static boolean DEBUG = true;
    public static final float PRINT_STATE_INTERVAL = 0;

    /** general settings */
    public static final String GAME_NAME = "Jet Fighter Game"; // laaaame
    public static final short TARGET_TPS = 19;

    /** connection settings */
    public static final int SERVER_PORT = 3039;

    /** collision detection */
    public static final int MAX_COLLISION_ITERATIONS = 100 / TARGET_TPS;
    private static final float BUMPOFF_SPEED = 10f;
    public static final float BUMPOFF_ENERGY = (0.5f * JetBasic.MASS * BUMPOFF_SPEED * BUMPOFF_SPEED); // e = 0.5*m*v*v in joule

    /** miscellaneous */
    public static final float COUNT_DOWN = 3f;
    public static final int NOF_FUN = 6;
    public static final int INTERPOLATION_QUEUE_SIZE = 120 / TARGET_TPS + 10;
    public static final float POWERUP_COLLECTION_RANGE = 14f;
    public static boolean RENDER_ENABLED = false;
    public static final float GENERAL_SPEED_FACTOR = 1.3f;

    public static final int CUBE_SIZE_LARGE = 25;
    public static final float CUBE_MASS_LARGE = CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * 0.001f;
}
