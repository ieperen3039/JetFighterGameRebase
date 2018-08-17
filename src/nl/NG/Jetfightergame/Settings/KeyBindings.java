package nl.NG.Jetfightergame.Settings;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public final class KeyBindings {
    public static final int MOUSE_BUTTON_RIGHT = GLFW_MOUSE_BUTTON_RIGHT;
    public static final int MOUSE_BUTTON_LEFT = GLFW_MOUSE_BUTTON_LEFT;
    public static final int MOUSE_BUTTON_MIDDLE = GLFW_MOUSE_BUTTON_MIDDLE;
    public static final int MOUSE_BUTTON_NONE = -1;

    public static final int XBOX_BUTTON_A = 0;
    public static final int XBOX_BUTTON_B = 1;
    public static final int XBOX_BUTTON_X = 2;
    public static final int XBOX_BUTTON_Y = 3;
    public static final int XBOX_BUTTON_LB = 4;
    public static final int XBOX_BUTTON_RB = 5;
    public static final int XBOX_BUTTON_BACK = 6;
    public static final int XBOX_BUTTON_START = 7;
    public static final int XBOX_BUTTON_LS = 8;
    public static final int XBOX_BUTTON_RS = 9;
    public static final int XBOX_BUTTON_UP = 10;
    public static final int XBOX_BUTTON_RIGHT = 11;
    public static final int XBOX_BUTTON_DOWN = 12;
    public static final int XBOX_BUTTON_LEFT = 13;

    public static final int XBOX_AXIS_LS_HOR = 0;
    public static final int XBOX_AXIS_LS_VERT = 1;
    public static final int XBOX_AXIS_RS_HOR = 2;
    public static final int XBOX_AXIS_RS_VERT = 3;
    public static final int XBOX_AXIS_LT = 4;
    public static final int XBOX_AXIS_RT = 5;

    public static int EXIT_GAME = GLFW_KEY_ESCAPE;
    public static int TOGGLE_FULLSCREEN = GLFW_KEY_F11;
    public static int PRINT_SCREEN = GLFW_KEY_PRINT_SCREEN;
    public static int START_GAME = GLFW_KEY_ENTER;
    public static int TOGGLE_DEBUG_SCREEN = GLFW_KEY_F3;

    public static int KEY_THROTTLE_UP = GLFW_KEY_W;
    public static int KEY_THROTTLE_DOWN = GLFW_KEY_S;
    public static int KEY_YAW_UP = GLFW_KEY_A;
    public static int KEY_YAW_DOWN = GLFW_KEY_D;
    public static int KEY_PRIMARY_FIRE = MOUSE_BUTTON_LEFT;
    public static int KEY_SECONDARY_FIRE = MOUSE_BUTTON_RIGHT;
}
