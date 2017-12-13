package nl.NG.Jetfightergame.Controllers;

import static nl.NG.Jetfightergame.Engine.Settings.PITCH_MODIFIER;
import static nl.NG.Jetfightergame.Engine.Settings.ROLL_MODIFIER;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * this controller considers the mouse to give absolute roll and pitch values.
 * Not moving the mouse will result in not rolling/pitching the plane
 */
public class PlayerPCControllerAbsolute extends PlayerPCController {
    private static final float SENSITIVITY = 1f;

    public PlayerPCControllerAbsolute() {
        super();
    }

    @Override
    public float pitch() {
        return normalize(currentPitch * PITCH_MODIFIER * SENSITIVITY);
    }

    @Override
    public float roll() {
        return normalize(currentRoll * ROLL_MODIFIER * SENSITIVITY);
    }

    /** returns some sigmoid function of the given float */
    private float normalize(float val){
        return (val / (1 + Math.abs(val)));
    }
}
