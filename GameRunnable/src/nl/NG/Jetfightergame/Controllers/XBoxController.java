package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Settings.KeyBinding;
import nl.NG.Jetfightergame.Tools.KeyNameMapper;
import nl.NG.Jetfightergame.Tools.Logger;

import static nl.NG.Jetfightergame.Tools.KeyNameMapper.XBOX_BUTTON_NONE;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public class XBoxController implements Controller {

    private int xbox;
    private final byte[] buttons = new byte[14];
    private final float[] axes = new float[6];
    private float[] t = new float[1];

    public XBoxController() {
        xbox = GLFW_JOYSTICK_1;

        while (xbox <= GLFW_JOYSTICK_LAST && !glfwJoystickPresent(xbox)) xbox++;
        if (xbox > GLFW_JOYSTICK_LAST) {
            Logger.ERROR.print("No controller connected!");
            return;
        }

        update();
    }

    @Override
    public void update() {
        if (xbox > GLFW_JOYSTICK_LAST) {
            xbox = GLFW_JOYSTICK_1;
            while (xbox <= GLFW_JOYSTICK_LAST && !glfwJoystickPresent(xbox)) xbox++;
            if (xbox > GLFW_JOYSTICK_LAST) return;
        }

        glfwGetJoystickButtons(xbox).get(buttons);
        glfwGetJoystickAxes(xbox).get(axes);
    }

    @Override
    public float throttle() {
        return readKey(KeyBinding.THROTTLE, KeyBinding.BREAK);
    }

    @Override
    public float pitch() {
        return readKey(KeyBinding.PITCH_UP, KeyBinding.PITCH_DOWN);
    }

    @Override
    public float yaw() {
        return readKey(KeyBinding.YAW_RIGHT, KeyBinding.YAW_LEFT);
    }

    @Override
    public float roll() {
        return readKey(KeyBinding.ROLL_RIGHT, KeyBinding.ROLL_LEFT);
    }

    @Override
    public boolean primaryFire() {
        return readKey(KeyBinding.PRIMARY_FIRE, KeyBinding.NO_ACTION) > 0.5f; // support for stupid decisions?
    }

    @Override
    public boolean secondaryFire() {
        return readKey(KeyBinding.SECONDARY_FIRE, KeyBinding.NO_ACTION) > 0.5f;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }

    /**
     * returns the value represented by the axis [downAction - upAction]
     */
    private float readKey(KeyBinding upAction, KeyBinding downAction) {
        if (upAction.isXBoxAxis()) {
            int upAxis = upAction.getXBox();
            float upValue = KeyNameMapper.readAxis(upAxis, axes);

            int downAxis = downAction.getXBox();
            float downValue = KeyNameMapper.readAxis(downAxis, axes);

            return (upValue - downValue) / 2; // axes are in range [-1, 1]
        }

//        boolean isUp = buttons[upButton] == GLFW_PRESS;
//        boolean isDown = buttons[downButton] == GLFW_PRESS;
//        return isUp == isDown ? 0 : (isUp ? 1 : -1);

        int upButton = upAction.getXBox();
        int downButton = downAction.getXBox();

        byte upValue = upButton == XBOX_BUTTON_NONE ? 0 : buttons[upButton];
        byte downValue = downButton == XBOX_BUTTON_NONE ? 0 : buttons[downButton];

        return (upValue - downValue) / GLFW_PRESS;
    }
}
