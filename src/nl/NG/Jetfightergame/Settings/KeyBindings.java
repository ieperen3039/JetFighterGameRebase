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

    public static final int EXIT_GAME = GLFW_KEY_ESCAPE;
    public static final int TOGGLE_FULLSCREEN = GLFW_KEY_F11;

    public static int THROTTLE_UP = GLFW_KEY_W;
    public static int THROTTLE_DOWN = GLFW_KEY_S;
    public static int YAW_UP = GLFW_KEY_A;
    public static int YAW_DOWN = GLFW_KEY_D;
}
