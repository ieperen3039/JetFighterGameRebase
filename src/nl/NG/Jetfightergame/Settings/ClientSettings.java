package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public final class ClientSettings {
    public static final short TARGET_FPS = 60;

    /** engine settings */
    // rendering is delayed by RENDER_DELAY seconds to smoothen rendering and prevent extrapolation of the gamestate
    public static final float RENDER_DELAY = 1f / ServerSettings.TARGET_TPS;
    public static final boolean LOCAL_SERVER = true;

    /** sound */
    public static final float SOUND_MASTER_GAIN = 0;
    public static final float MAX_VOLUME = 6f;
    public static final float MIN_VOLUME = -20f;
    public static boolean SAVE_PLAYBACK = false;

    /** controller settings; these modifiers are also used to inverse direction */
    public static float PITCH_MODIFIER = -0.05f;
    public static float ROLL_MODIFIER = 0.05f;
    public static final int CONNECTION_SEND_FREQUENCY = ServerSettings.TARGET_TPS;

    /** visual settings */
    public static float FOV = (float) Math.toRadians(60);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 4000.0f;
    public static final boolean CULL_FACES = true;
    public static Boolean INVERT_CAMERA_ROTATION = false;
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 1;
    public static final int MAX_POINT_LIGHTS = 10;
    public static final boolean SHOW_LIGHT_POSITIONS = true;
    public static final boolean ITERATIVE_ROTATION_INTERPOLATION = true;
    public static float HIGHLIGHT_LINE_WIDTH = 1f;
    public static final Color4f CHECKPOINT_ACTIVE_COLOR = Color4f.YELLOW;

    /** particle settings */
    public static final int EXPLOSION_PARTICLE_DENSITY = 1000; // particles in total
    public static final float FIRE_PARTICLE_SIZE = 0.8f;
    public static final Color4f EXPLOSION_COLOR_1 = Color4f.RED;
    public static final Color4f EXPLOSION_COLOR_2 = Color4f.YELLOW;
    public static final int PARTICLE_SPLITS = 2;
    public static final int PARTICLECLOUD_SPLIT_SIZE = 2000;
    public static final float PARTICLECLOUD_MIN_TIME = 0.5f;

    /** thrust particle settings */
    public static final float THRUST_PARTICLE_SIZE = 0.8f;
    public static final float THRUST_PARTICLE_LINGER_TIME = 0.5f;
    public static final float THRUST_PARTICLES_PER_SECOND = 150f;
    public static final float JET_THRUST_SPEED = 80f;
    public static final float ROCKET_THRUST_SPEED = 50f;
    public static final Color4f THRUST_COLOR_1 = Color4f.ORANGE;
    public static final Color4f THRUST_COLOR_2 = Color4f.RED;

    /** miscellaneous */
    public static boolean SPECTATOR_MODE = false;
    public static final Material PORTAL_MATERIAL = Material.PLASTIC;
}
