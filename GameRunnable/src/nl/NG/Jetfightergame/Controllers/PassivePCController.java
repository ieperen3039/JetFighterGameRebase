package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerMoveListener;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.KeyBinding;

import static nl.NG.Jetfightergame.Settings.KeyBinding.*;
import static nl.NG.Jetfightergame.Tools.KeyNameMapper.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;

/**
 * @author Geert van Ieperen created on 31-10-2017. TODO multiply output with how long the button was pressed in respect
 *         to deltaTime TODO create keyboard and mouse tracker combined
 */
public abstract class PassivePCController implements TrackerMoveListener, Controller {

    protected KeyTracker keyboard;
    protected MouseTracker mouse;
    protected int mouseX;
    protected int mouseY;

    protected PassivePCController() {
        keyboard = KeyTracker.getInstance();
        mouse = MouseTracker.getInstance();

        mouse.addMotionListener(this);
    }

    @Override
    public void update() {
        // Poll for events at the active window
        glfwPollEvents();
    }

    @Override
    public float throttle() {
        return readKey(THROTTLE, BREAK, ClientSettings.THROTTLE_MODIFIER);
    }

    @Override
    public float pitch() {
        return readKey(PITCH_UP, PITCH_DOWN, ClientSettings.PITCH_MODIFIER);
    }

    @Override
    public float yaw() {
        return readKey(YAW_RIGHT, YAW_LEFT, ClientSettings.YAW_MODIFIER);
    }

    @Override
    public float roll() {
        return readKey(ROLL_RIGHT, ROLL_LEFT, ClientSettings.ROLL_MODIFIER);
    }

    @Override
    public boolean primaryFire() {
        return readKey(KeyBinding.PRIMARY_FIRE, KeyBinding.NO_ACTION, 1f / GLFW_PRESS) > 0.5f; // support for stupid decisions?
    }

    @Override
    public boolean secondaryFire() {
        return readKey(KeyBinding.SECONDARY_FIRE, KeyBinding.NO_ACTION, 1f / GLFW_PRESS) > 0.5f; // support for stupid decisions?
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        mouseX += deltaX;
        // up in y (screen coordinates) is down for the mouse
        mouseY -= deltaY;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }

    @Override
    public void cleanUp() {
        MouseTracker.getInstance().removeMotionListener(this);
    }

    /**
     * calls the {@link #getMouseX(float)}, {@link #getMouseY(float)} or {@link #getKeyAxis(KeyBinding, KeyBinding)}
     * method for the given key-binding
     */
    private float readKey(KeyBinding upAction, KeyBinding downAction, float modifier) {
        if (upAction.isMouseAxis()) {
            if (upAction.is(MOUSE_UP) && downAction.is(MOUSE_DOWN)) {
                return getMouseY(modifier);
            } else if (upAction.is(MOUSE_RIGHT) && downAction.is(MOUSE_LEFT)) {
                return getMouseX(modifier);
            } else if (upAction.is(MOUSE_DOWN) && downAction.is(MOUSE_UP)) { // up == down
                return -getMouseY(modifier); // inversed
            } else if (upAction.is(MOUSE_LEFT) && downAction.is(MOUSE_RIGHT)) {
                return -getMouseX(modifier); // inversed
            } else {
                throw new IllegalArgumentException("Mouse actions are not opposites");
            }
        }
        return getKeyAxis(upAction, downAction);
    }

    protected abstract float getKeyAxis(KeyBinding keyUp, KeyBinding keyDown);

    protected abstract float getMouseY(float modifier);

    protected abstract float getMouseX(float modifier);

    /** returns some sigmoid function of the given float */
    protected float normalize(float val) {
        return (val / (1 + Math.abs(val)));
    }
}

