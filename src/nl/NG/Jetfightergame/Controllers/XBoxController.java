package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Tools.Logger;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public class XBoxController implements Controller {

    public XBoxController() {
        int xbox = GLFW_JOYSTICK_1;

        if (!glfwJoystickPresent(xbox)) {
            Logger.printError("No controller connected!");
            return;
        }

        byte[] buttonPresses = new byte[14];
        float[] axes = new float[6];

        Logger.printOnline(() -> {
            glfwGetJoystickButtons(xbox).get(buttonPresses);
            return "Buttons: " + Arrays.toString(buttonPresses);
        });
        Logger.printOnline(() -> {
            glfwGetJoystickAxes(xbox).get(axes);
            return "Axes: " + Arrays.toString(axes);
        });
    }

    @Override
    public float throttle() {
        return 0;
    }

    @Override
    public float pitch() {
        return 0;
    }

    @Override
    public float yaw() {
        return 0;
    }

    @Override
    public float roll() {
        return 0;
    }

    @Override
    public boolean primaryFire() {
        return false;
    }

    @Override
    public boolean secondaryFire() {
        return false;
    }

}
