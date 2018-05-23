package nl.NG.Jetfightergame.Settings;

import org.lwjgl.glfw.GLFW;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public final class KeyBindings {
    public static final int MOUSE_BUTTON_RIGHT = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    public static final int MOUSE_BUTTON_LEFT = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    public static final int MOUSE_BUTTON_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    public static final int MOUSE_BUTTON_NONE = -1;
    public static int THROTTLE_UP = GLFW.GLFW_KEY_W;
    public static int THROTTLE_DOWN = GLFW.GLFW_KEY_S;
    public static int YAW_UP = GLFW.GLFW_KEY_A;
    public static int YAW_DOWN = GLFW.GLFW_KEY_D;
}
