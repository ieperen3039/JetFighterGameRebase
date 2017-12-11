package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerClickListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerMoveListener;

import static java.awt.event.KeyEvent.*;
import static nl.NG.Jetfightergame.Engine.Settings.*;

/**
 * @author Geert van Ieperen
 *         created on 31-10-2017.
 */
public class PlayerController implements Controller, TrackerMoveListener, TrackerClickListener {

    private KeyTracker keyboard;
    private MouseTracker mouse;
    private int currentRoll;
    private int currentPitch;
    private boolean lazyButtonLeft;
    private boolean lazyButtonRight;

    public PlayerController() {
        keyboard = KeyTracker.getInstance();
        mouse = MouseTracker.getInstance();
        mouse.addMotionListener(this);
        mouse.addClickListener(this, true);
        keyboard.addKey(VK_W);
        keyboard.addKey(VK_A);
        keyboard.addKey(VK_S);
        keyboard.addKey(VK_D);
    }

    @Override
    public float throttle() {
        int i = 0;
        if (keyboard.isPressed(THROTTLE_UP)) i++;
        if (keyboard.isPressed(THROTTLE_DOWN)) i--;
        return (i);
    }

    @Override
    public float pitch() {
        return currentPitch;
    }

    @Override
    public float yaw() {
        int i = 0;
        if (keyboard.isPressed(YAW_UP)) i++;
        if (keyboard.isPressed(YAW_DOWN)) i--;
        return (i);
    }

    @Override
    public int roll() {
        return currentRoll;
    }

    @Override
    public boolean primaryFire() {
        if (lazyButtonRight) {
            lazyButtonRight = false;
            return true;
        }
        return mouse.rightButton();
    }

    @Override
    public boolean secondaryFire() {
        if (lazyButtonLeft) {
            lazyButtonLeft = false;
            return true;
        }
        return mouse.leftButton();
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        currentPitch += deltaX * PITCH_MODIFIER;
        // up in y (screen coordinates) is down in plane
        currentRoll -= deltaY * ROLL_MODIFIER;
    }

    @Override
    public void cleanUp() {
        MouseTracker.getInstance().removeClickListener(this, true);
        MouseTracker.getInstance().removeMotionListener(this);
    }

    /**
     * catch quick-clicks by utilizing a 'lazy' button
     */
    @Override
    public void clickEvent(int x, int y) {
        if (mouse.leftButton()) lazyButtonLeft = true;
        else if (mouse.rightButton()) lazyButtonRight = true;
    }
}

