package nl.NG.Jetfightergame.Settings;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public final class ClientSettings {
    public static final short TARGET_FPS = 60;

    /** engine settings */
    // rendering is delayed by RENDER_DELAY seconds to smoothen rendering and prevent extrapolation of the gamestate
    public static final float RENDER_DELAY = 2f/ ServerSettings.TARGET_TPS;
    public static final int COLLISION_DETECTION_LEVEL = 0;
    public static final int PHYSICS_TPS = 30;
    public static final boolean LOCAL_SERVER = true;
    public static boolean FIXED_DELTA_TIME = false;

    /** particles */
    public static final int FIRE_PARTICLE_DENSITY = 1000; // actual particle count of plane explosion
    public static final int SPARK_PARTICLE_DENSITY = 10;
    public static final float FIRE_PARTICLE_SIZE = 0.3f;
    public static final int PARTICLE_SPLITS = 0;

    /** sound */
    public static final float SOUND_MASTER_GAIN = 0;
    public static final float MAX_VOLUME = 6f;
    public static final float MIN_VOLUME = -20f;
    public static float HIGHLIGHT_LINE_WIDTH = 1f;
    public static boolean SAVE_PLAYBACK = false;

    /** controller settings; these modifiers are also used to inverse direction */
    public static float PITCH_MODIFIER = -0.05f;
    public static float ROLL_MODIFIER = 0.05f;
    public static final int CONNECTION_SEND_FREQUENCY = 60;
    public static boolean SPECTATOR_MODE = false;

    /** visual settings */
    public static float FOV = (float) Math.toRadians(60);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 2000.0f;
    public static final boolean CULL_FACES = true;
    public static Boolean INVERT_CAMERA_ROTATION = false;
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 3;
    public static final int MAX_POINT_LIGHTS = 10;
    public static final boolean SHOW_LIGHT_POSITIONS = true;
}
