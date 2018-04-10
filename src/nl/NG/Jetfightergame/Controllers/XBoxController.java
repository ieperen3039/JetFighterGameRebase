package nl.NG.Jetfightergame.Controllers;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public class XBoxController implements Controller {

    public XBoxController() {

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
