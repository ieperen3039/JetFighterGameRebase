package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerClickListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerMoveListener;
import nl.NG.Jetfightergame.Settings.KeyBindings;

/**
 * @author Geert van Ieperen
 *         created on 31-10-2017.
 *         TODO multiply output with how long the button was pressed in respect to deltaTime
 */
public abstract class PlayerPCController implements TrackerMoveListener, TrackerClickListener, Controller {

    private KeyTracker keyboard;
    private MouseTracker mouse;
    protected int currentRoll;
    protected int currentPitch;
    private boolean stickyButtonLeft = false;
    private boolean stickyButtonRight = false;

    protected PlayerPCController() {
        keyboard = KeyTracker.getInstance();
        mouse = MouseTracker.getInstance();

        mouse.addMotionListener(this);
        mouse.addClickListener(this, true);
        keyboard.addKey(KeyBindings.THROTTLE_UP);
        keyboard.addKey(KeyBindings.THROTTLE_DOWN);
        keyboard.addKey(KeyBindings.YAW_UP);
        keyboard.addKey(KeyBindings.YAW_DOWN);
    }

    @Override
    public float throttle() {
        int i = 0;
        if (keyboard.isPressed(KeyBindings.THROTTLE_UP)) i++;
        if (keyboard.isPressed(KeyBindings.THROTTLE_DOWN)) i--;
        return (i);
    }

    @Override
    public float yaw() {
        int i = 0;
        if (keyboard.isPressed(KeyBindings.YAW_UP)) i++;
        if (keyboard.isPressed(KeyBindings.YAW_DOWN)) i--;
        return (i);
    }

    @Override
    public boolean primaryFire() {
        if (stickyButtonLeft) {
            stickyButtonLeft = false;
            return true;
        }
        return mouse.leftButton();
    }

    @Override
    public boolean secondaryFire() {
        if (stickyButtonRight) {
            stickyButtonRight = false;
            return true;
        }
        return mouse.rightButton();
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        currentRoll += deltaX;
        // up in y (screen coordinates) is down in 3d, and this is up again for a plane
        currentPitch += deltaY;
    }

    @Override
    public void cleanUp() {
        MouseTracker.getInstance().removeClickListener(this, true);
        MouseTracker.getInstance().removeMotionListener(this);
    }

    /**
     * catch quick-clicks by utilizing a 'sticky' button
     */
    @Override
    public void clickEvent(int x, int y) {
        if (mouse.leftButton()) stickyButtonLeft = true;
        else if (mouse.rightButton()) stickyButtonRight = true;
    }

    /** returns some sigmoid function of the given float */
    protected float normalize(float val){
        return (val / (1 + Math.abs(val)));
    }
}

