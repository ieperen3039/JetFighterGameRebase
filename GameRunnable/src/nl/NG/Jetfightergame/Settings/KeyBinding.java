package nl.NG.Jetfightergame.Settings;

import java.util.EnumSet;

import static nl.NG.Jetfightergame.Tools.KeyNameMapper.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public enum KeyBinding {
    NO_ACTION(GLFW_KEY_UNKNOWN, false, XBOX_BUTTON_NONE, false),

    START_GAME(GLFW_KEY_ENTER, false, XBOX_BUTTON_START, false),
    EXIT_GAME(GLFW_KEY_ESCAPE, false, XBOX_BUTTON_X, false),
    PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN, false, XBOX_BUTTON_UP, false),
    TOGGLE_FULLSCREEN(GLFW_KEY_F11, false, XBOX_BUTTON_RIGHT, false),
    TOGGLE_DEBUG_SCREEN(GLFW_KEY_F3, false, XBOX_BUTTON_LEFT, false),
    DISABLE_HUD(GLFW_KEY_H, false, XBOX_BUTTON_DOWN, false),

    PITCH_UP(MOUSE_DOWN, true, XBOX_AXIS_LS_DOWN, true),
    PITCH_DOWN(MOUSE_UP, true, XBOX_AXIS_LS_UP, true),
    ROLL_RIGHT(MOUSE_RIGHT, true, XBOX_AXIS_LS_RIGHT, true),
    ROLL_LEFT(MOUSE_LEFT, true, XBOX_AXIS_LS_LEFT, true),
    THROTTLE(GLFW_KEY_W, false, XBOX_AXIS_LT_UP, true),
    BREAK(GLFW_KEY_S, false, XBOX_AXIS_RT_UP, true),
    YAW_LEFT(GLFW_KEY_A, false, XBOX_BUTTON_LB, false),
    YAW_RIGHT(GLFW_KEY_D, false, XBOX_BUTTON_RB, false),
    PRIMARY_FIRE(GLFW_KEY_SPACE, false, XBOX_BUTTON_A, false),
    SECONDARY_FIRE(GLFW_KEY_LEFT_SHIFT, false, XBOX_BUTTON_B, false);

    private static EnumSet<KeyBinding> actionButtons = EnumSet.of(
            EXIT_GAME, TOGGLE_FULLSCREEN, PRINT_SCREEN, START_GAME, TOGGLE_DEBUG_SCREEN, DISABLE_HUD);

    private static EnumSet<KeyBinding> controlButtons = EnumSet.of(
            PITCH_UP, PITCH_DOWN, ROLL_LEFT, ROLL_RIGHT, THROTTLE, BREAK,
            YAW_LEFT, YAW_RIGHT, PRIMARY_FIRE, SECONDARY_FIRE);

    public static EnumSet<KeyBinding> getActionButtons() {
        return EnumSet.copyOf(actionButtons);
    }

    public static EnumSet<KeyBinding> getControlButtons() {
        return EnumSet.copyOf(controlButtons);
    }

    private int key;
    private boolean isMouseAxis;
    private int xBoxButton;
    private boolean isXBoxAxis;

    KeyBinding(int value, boolean isMouseAxis, int xBoxButton, boolean isXBoxAxis) {
        this.key = value;
        this.isMouseAxis = isMouseAxis;
        this.xBoxButton = xBoxButton;
        this.isXBoxAxis = isXBoxAxis;
    }

    public int getKey() {
        return key;
    }

    public int getXBox() {
        return xBoxButton;
    }

    public boolean isMouseAxis() {
        return isMouseAxis;
    }

    public boolean isXBoxAxis() {
        return isXBoxAxis;
    }

    public boolean is(int i) {
        return key == i;
    }

    public String keyName() {
        if (isMouseAxis) return getMouseAxisName(key);
        else return getKeyName(key);
    }

    public String xBoxName() {
        return "XBOX_" + (isXBoxAxis ? "AXIS_" : "") + xBoxButton;
    }

    /**
     * sets the xbox binding or the keyboard binding of this key to the new value
     * @param k      the new key code
     * @param isAxis true if the key code represents an axis, false if it represents a binary input (button)
     * @param isXBox true if the key code represents and xbox controller value, false if it represents keyboard or mouse
     *               values
     */
    public void installNew(int k, boolean isAxis, boolean isXBox) {
        if (isXBox) {
            xBoxButton = k;
            isXBoxAxis = isAxis;
        } else {
            key = k;
            isMouseAxis = isAxis;
        }
    }
}
