package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.Tools.Manager;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class ControllerManager implements Controller, Manager<ControllerManager.ControllerImpl> {

    private Controller instance = new PlayerPCControllerAbsolute();

    @Override
    public ControllerImpl[] implementations() {
        return ControllerImpl.values();
    }

    public enum ControllerImpl {
        MouseAbsolute, MouseRelative
    }

    public void switchTo(ControllerImpl type){
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }

        switch (type){
            case MouseAbsolute:
                instance = new PlayerPCControllerAbsolute();
                break;
            case MouseRelative:
                instance = new PlayerPCControllerRelative();
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
