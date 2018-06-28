package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Manager;

import java.util.function.Consumer;

/**
 * a controller decorator that manages the current controller for the player, implementing overriding control.
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class ControllerManager implements Controller, Manager<ControllerManager.ControllerImpl> {

    private ScreenOverlay hud = null;
    private Controller instance = new PassivePCControllerAbsolute();

    /**
     * all control types available for the player. This logically excludes AI.
     */
    public enum ControllerImpl {
        MouseAbsolute, MouseRelative, XboxController,
        EmptyController,
    }

    @Override
    public ControllerImpl[] implementations() {
        return ControllerImpl.values();
    }

    public void switchTo(ControllerImpl type){
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }

        if (hud != null) hud.removeHudItem(instance.hudElement());

        switch (type){
            case MouseAbsolute:
                instance = new PassivePCControllerAbsolute();
                break;
            case MouseRelative:
                instance = new PassivePCControllerRelative();
                break;
            case XboxController:
                instance = new XBoxController();
                break;
            case EmptyController:
                instance = new EmptyController();
                break;
            default:
                throw new UnsupportedOperationException("unknown enum: " + type);
        }

        if (hud != null) hud.addHudItem(instance.hudElement());
    }

    public void setDisplay(ScreenOverlay target) {
        if (hud != null) hud.removeHudItem(instance.hudElement());
        hud = target;
        hud.addHudItem(instance.hudElement());
    }

    @Override
    public float throttle() {
        return instance.throttle();
    }

    @Override
    public Consumer<ScreenOverlay.Painter> hudElement() {
        return instance.hudElement();
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
