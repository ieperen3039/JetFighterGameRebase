package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Vectors.Color4f;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 * a class that harbours the variables that may or may not be changed by the player
 */
public final class Settings {
    // final settings
    public static final String GAME_NAME = "Jet Fighter Game"; // laaaame
    public static boolean SPECTATOR_MODE = true;

    /** universal random to be used everywhere */
    public static final Random random = new Random();
    public static final boolean DEBUG = true;
    public static final boolean CULL_FACES = true;
    public static final Color4f HUD_COLOR = new Color4f(0.1f, 0.9f, 0.1f, 0.9f);
    public static final int MAX_COLLISION_ITERATIONS = 2000;
    public static final boolean SHOW_LIGHT_POSITIONS = true;
    public static final boolean ADVANCED_COLLISION_RESPONSE = false;
    public static boolean GYRO_PHYSICS_MODEL = false;
    public static boolean FIXED_DELTA_TIME = false;

    // final music settings
    public static final float MAX_VOLUME = 6f;
    public static final float MIN_VOLUME = -20f;

    // engine settings
    public static int TARGET_FPS = 60;
    public static int TARGET_TPS = 10;
    /** rendering is delayed by {@code RENDER_DELAY} seconds to smoothen rendering and prevent extrapolation of the gamestate*/
    public static final float RENDER_DELAY = 1.5f/TARGET_TPS;

    // controller settings
    public static int THROTTLE_UP = GLFW.GLFW_KEY_W;
    public static int THROTTLE_DOWN = GLFW.GLFW_KEY_S;
    public static int YAW_UP = GLFW.GLFW_KEY_A;
    public static int YAW_DOWN = GLFW.GLFW_KEY_D;
    // these modifiers are also used to inverse direction
    public static float PITCH_MODIFIER = -0.05f;
    public static float ROLL_MODIFIER = 0.05f;

    // camera settings
    public static final float CAMERA_CATCHUP = 0.9f;
    public static float FOV = (float) Math.toRadians(60);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 2000.0f;
    public static Boolean INVERT_CAMERA_ROTATION = true;

    // visual settings
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 3;
    public static final int MAX_POINT_LIGHTS = 10;
    public static final int HUD_STROKE_WIDTH = 0;
}
