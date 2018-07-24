package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.BasicJet;

import static nl.NG.Jetfightergame.Settings.ClientSettings.BASE_SPEED;

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
    public static final short TARGET_TPS = 19;
    public static final short COLLISION_DETECTION_LEVEL = 0;

    /** connection settings */
    public static final int SERVER_PORT = 3039;

    /** collision detection */
    public static final int MAX_COLLISION_ITERATIONS = 100 / TARGET_TPS;
    public static final float BASE_BUMPOFF_ENERGY = (BasicJet.MASS * BASE_SPEED * BASE_SPEED * 0.01f); // e = 0.5*m*v*v in joule

    /** miscellaneous */
    public static final boolean FUN = true;
    public static final int INTERPOLATION_QUEUE_SIZE = 120 / TARGET_TPS + 10;
    public static final int PLAYERS_PER_RACE = 8;
    public static final float POWERUP_COLLECTION_RANGE = 5;
    public static final int NOF_SEEKERS_LAUNCHED = 20;
    public static boolean RENDER_ENABLED = false;

    public static final int CUBE_SIZE_LARGE = 25;
    public static final float CUBE_MASS_LARGE = CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * 0.001f;
}
