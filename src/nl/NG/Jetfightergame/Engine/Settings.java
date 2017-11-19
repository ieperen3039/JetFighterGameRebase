package nl.NG.Jetfightergame.Engine;

import org.joml.Vector3f;

import java.awt.*;
import java.awt.event.KeyEvent;
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

    // final music settings
    public static final float MAX_VOLUME = 6f;
    public static final float MIN_VOLUME = -20f;
    public static final float SHADER_SPECULAR_POWER = 2f;

    // engine settings
    public static int TARGET_FPS = 60;
    public static int TARGET_TPS = 20;

    // controller settings
    public static int THROTTLE_UP = KeyEvent.VK_W;
    public static int THROTTLE_DOWN = KeyEvent.VK_S;
    public static int YAW_UP = KeyEvent.VK_D;
    public static int YAW_DOWN = KeyEvent.VK_A;
    public static float PITCH_MODIFIER = 1f;
    public static float ROLL_MODIFIER = 1f;

    // camera settings
    public static int FOV = (int) Math.toRadians(60.0f);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 500.0f;
    public static float PHI_MIN = -(float) Math.PI / 2f + 0.01f;
    public static float PHI_MAX = (float) Math.PI / 2f - 0.01f;
    public static Boolean INVERT_CAMERA_ROTATION = false;
    public static float VDIST = 10f;
    // Ratio of distance in pixels dragged and radial change of camera.
    public static float DRAG_PIXEL_TO_RADIAN = 0.025f;

    // visual settings
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 4;
    public static Vector3f AMBIENT_LIGHT = colorToVector(Color.LIGHT_GRAY);

    private static Vector3f colorToVector(Color color){
        return new Vector3f(color.getRed(), color.getGreen(), color.getBlue());
    }

}
