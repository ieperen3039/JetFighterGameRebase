package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Assets.FighterJets.BasicJet;

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
    public static final short COLLISION_DETECTION_LEVEL = 1;

    /** connection settings */
    public static final int SERVER_PORT = 3039;

    /** collision detection */
    public static final int MAX_COLLISION_ITERATIONS = 100 / TARGET_TPS;
    public static final float BASE_BUMPOFF_ENERGY = (BasicJet.MASS * AbstractJet.BASE_SPEED * AbstractJet.BASE_SPEED * 0.01f); // e = 0.5*m*v*v in joule

    /** miscellaneous */
    public static final int INTERPOLATION_QUEUE_SIZE = 10;
    public static boolean RENDER_ENABLED = false;

    /** shape constants */
    public static final int CUBE_SIZE_SMALL = 2;
    public static final int CUBE_SIZE_LARGE = 50;
    public static final float CUBE_MASS_SMALL = CUBE_SIZE_SMALL * CUBE_SIZE_SMALL * CUBE_SIZE_SMALL * 0.001f;
    public static final float CUBE_MASS_LARGE = CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * CUBE_SIZE_LARGE * 0.001f;
}
