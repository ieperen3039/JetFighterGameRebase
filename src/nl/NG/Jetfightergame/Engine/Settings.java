package nl.NG.Jetfightergame.Engine;

import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 * a class that harbours the variables that can be set by the user
 * and maybe some that can not be set by the user
 */
public class Settings {
    // final settings
    public static final String GAME_NAME = "Jet Fighter Game"; // laaaame

    /** universal random to be used everywhere */
    public static final Random random = new Random();
    public static final boolean DEBUG = true;
    public static final boolean UNIT_COLLISION = true;
    public static boolean RECURSIVE_COLLISION = true;

    // final music settings
    public static final float MAX_VOLUME = 6f;
    public static final float MIN_VOLUME = -20f;

    // engine settings
    public static int TARGET_FPS = 60;
    public static int TARGET_TPS = 25;

    // controller settings
    public static int THROTTLE_UP = GLFW.GLFW_KEY_W;
    public static int THROTTLE_DOWN = GLFW.GLFW_KEY_S;
    public static int YAW_UP = GLFW.GLFW_KEY_D;
    public static int YAW_DOWN = GLFW.GLFW_KEY_A;
    // these modifiers are also used to inverse direction
    public static float PITCH_MODIFIER = 1f;
    public static float ROLL_MODIFIER = 1f;

    // camera settings
    public static int FOV = (int) Math.toRadians(60.0f);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 500.0f;
    public static Boolean INVERT_CAMERA_ROTATION = true;

    // visual settings
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 4;
    public final static int MAX_POINT_LIGHTS = 10;
    /** rendering is delayed by {@code RENDER_DELAY} seconds to smoothen rendering and prevent extrapolation of the gamestate*/
    public static final float RENDER_DELAY = (float) (1 / (Math.ceil(TARGET_FPS/TARGET_TPS) + 1) / 1000);

}
