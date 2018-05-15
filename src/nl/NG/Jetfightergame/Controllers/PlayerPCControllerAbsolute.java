package nl.NG.Jetfightergame.Controllers;

import static nl.NG.Jetfightergame.Settings.ClientSettings.PITCH_MODIFIER;
import static nl.NG.Jetfightergame.Settings.ClientSettings.ROLL_MODIFIER;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * this controller considers the mouse to give absolute roll and pitch values.
 * Not moving the mouse will result in not rolling/pitching the plane
 */
public class PlayerPCControllerAbsolute extends PlayerPCController {
    private static final float SENSITIVITY = 1.0f;

    @Override
    public float pitch() {
        int pitch = currentPitch;
        currentPitch = 0;
        float normalize = normalize(pitch * PITCH_MODIFIER * SENSITIVITY);
//        if (normalize != 0) Toolbox.print(normalize);
        return normalize;
    }

    @Override
    public float roll() {
        int roll = currentRoll;
        currentRoll = 0;
        return normalize(roll * ROLL_MODIFIER * SENSITIVITY);
    }
}
