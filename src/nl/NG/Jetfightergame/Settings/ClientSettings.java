package nl.NG.Jetfightergame.Settings;

import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public final class ClientSettings {
    /** engine settings */
    public static int TARGET_FPS = 60;
    public static boolean DEBUG_SCREEN = false;
    // rendering is delayed by RENDER_DELAY seconds to smoothen rendering and prevent extrapolation of the gamestate
    public static float RENDER_DELAY = 1f / ServerSettings.TARGET_TPS;

    /** visual settings */
    public static float FOV = (float) Math.toRadians(60);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 4000.0f;
    public static boolean CULL_FACES = true;
    public static boolean INVERT_CAMERA_ROTATION = false;
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 1;
    public static boolean SHOW_LIGHT_POSITIONS = DEBUG_SCREEN;
    public static float HIGHLIGHT_LINE_WIDTH = 1f;
    public static Color4f CHECKPOINT_ACTIVE_COLOR = Color4f.YELLOW;
    public static final int MAX_POINT_LIGHTS = 10;

    /** controller settings; these modifiers are also used to inverse direction */
    public static float PITCH_MODIFIER = -0.05f;
    public static float ROLL_MODIFIER = 0.05f;
    public static int CONNECTION_SEND_FREQUENCY = ServerSettings.TARGET_TPS;

    /** sound */
    public static float SOUND_MASTER_GAIN = 0;
    public static float MAX_VOLUME = 6f;
    public static float MIN_VOLUME = -20f;

    /** particle settings */
    public static int EXPLOSION_PARTICLE_DENSITY = 1000; // particles in total
    public static float FIRE_PARTICLE_SIZE = 0.8f;
    public static Color4f EXPLOSION_COLOR_1 = Color4f.RED;
    public static Color4f EXPLOSION_COLOR_2 = Color4f.YELLOW;
    public static int PARTICLE_SPLITS = 2;
    public static int PARTICLECLOUD_SPLIT_SIZE = 2000;
    public static float PARTICLECLOUD_MIN_TIME = 0.5f;

    /** thrust particle settings */
    public static float THRUST_PARTICLE_SIZE = 0.8f;
    public static float THRUST_PARTICLE_LINGER_TIME = 0.5f;
    public static float THRUST_PARTICLES_PER_SECOND = 200f;
    public static float JET_THRUST_SPEED = 60f;
    public static float ROCKET_THRUST_SPEED = 50f;
    public static Color4f THRUST_COLOR_1 = Color4f.ORANGE;
    public static Color4f THRUST_COLOR_2 = Color4f.RED;

    /** miscellaneous */
    public static Material PORTAL_MATERIAL = Material.PLASTIC;
    public static EntityClass JET_TYPE = EntityClass.JET_SPITZ;
    public static final boolean USE_SOCKET_FOR_OFFLINE = false;
}
