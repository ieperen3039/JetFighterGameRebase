package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.Tools.Manager;

/**
 * a controller decorator that manages the current controller for the player, implementing overriding control.
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class ControllerManager implements Controller, Manager<ControllerManager.ControllerImpl> {

    private Controller playerControl = new PlayerPCControllerAbsolute();
    private Controller override = new EmptyController();

    private Controller instance = playerControl;

    /**
     * all control types available for the player. This logically excludes AI.
     */
    public enum ControllerImpl {
        MouseAbsolute, MouseRelative
    }

    @Override
    public ControllerImpl[] implementations() {
        return ControllerImpl.values();
    }

    /**
     * set the implementation of the override, but doesn't enables it.
     * @param override the new override implementation
     */
    public void setOverride(Controller override) {
        if (override instanceof TrackerListener) {
            ((TrackerListener) override).cleanUp();
        }
        this.override = override;
    }

    /**
     * @param enable if true, the player has control.
     *               if false, the override will have the control.
     */
    public void setPlayerControl(boolean enable){
        instance = enable ? playerControl : override;
    }

    public void switchTo(ControllerImpl type){
        if (playerControl instanceof TrackerListener) {
            ((TrackerListener) playerControl).cleanUp();
        }

        switch (type){
            case MouseAbsolute:
                playerControl = new PlayerPCControllerAbsolute();
                break;
            case MouseRelative:
                playerControl = new PlayerPCControllerRelative();
                break;
            default:
                throw new UnsupportedOperationException("unknown enum: " + type);
        }
    }

    @Override
    public float throttle() {
        return instance.throttle();
    }

    @Override
    public float pitch() {
        return instance.pitch();
    }

    @Override
    public float yaw() {
        return instance.yaw();
    }

    @Override
    public float roll() {
        return instance.roll();
    }

    @Override
    public boolean primaryFire() {
        return instance.primaryFire();
    }

    @Override
    public boolean secondaryFire() {
        return instance.secondaryFire();
    }
}
