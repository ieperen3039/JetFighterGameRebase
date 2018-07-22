package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Tools.Logger;

import static nl.NG.Jetfightergame.Settings.KeyBindings.*;
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

        if (!glfwJoystickPresent(xbox)) {
            Logger.ERROR.print("No controller connected!");
            return;
        }

        update();

        Logger.printOnline(() -> String.format("Throttle: %5f", t[0]));
    }

    @Override
    public void update() {
        glfwGetJoystickButtons(xbox).get(buttons);
        glfwGetJoystickAxes(xbox).get(axes);
    }

    @Override
    public float throttle() {
        // 1/2(RT + 1) - 1/2(LT + 1)
        t[0] = (axes[XBOX_AXIS_RT] - axes[XBOX_AXIS_LT]) / 2;
        return t[0];
    }

    @Override
    public float pitch() {
        return axes[XBOX_AXIS_LS_VERT];
    }

    @Override
    public float yaw() {
        boolean lt = buttons[XBOX_AXIS_LT] == GLFW_PRESS;
        boolean rt = buttons[XBOX_AXIS_RT] == GLFW_PRESS;

        if (lt == rt) {
            return 0;
        } else {
            return lt ? 1 : -1;
        }
    }

    @Override
    public float roll() {
        return axes[XBOX_AXIS_LS_HOR];
    }

    @Override
    public boolean primaryFire() {
        return buttons[XBOX_BUTTON_A] == GLFW_PRESS;
    }

    @Override
    public boolean secondaryFire() {
        return buttons[XBOX_BUTTON_B] == GLFW_PRESS;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }
}
