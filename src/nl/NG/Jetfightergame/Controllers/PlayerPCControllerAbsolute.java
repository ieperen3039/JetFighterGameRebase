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
    private static final float SENSITIVITY = 0.5f;

    @Override
    public float pitch() {
        int pitch = currentPitch;
        currentPitch = 0;
        return normalize(pitch * PITCH_MODIFIER * SENSITIVITY);
    }

    @Override
    public float roll() {
        int roll = currentRoll;
        currentRoll = 0;
        return normalize(roll * ROLL_MODIFIER * SENSITIVITY);
    }
}
