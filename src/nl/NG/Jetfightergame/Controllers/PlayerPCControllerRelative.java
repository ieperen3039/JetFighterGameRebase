package nl.NG.Jetfightergame.Controllers;

import static nl.NG.Jetfightergame.Engine.Settings.PITCH_MODIFIER;
import static nl.NG.Jetfightergame.Engine.Settings.ROLL_MODIFIER;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * considers the position of the mouse to be relative to the middle (initial position)
 * Not moving the mouse will result in a continuous roll/pitch of the plane
 */
public class PlayerPCControllerRelative extends PlayerPCController {
    private static final float SENSITIVITY = 1f;

    @Override
    public float pitch() {
        return normalize(currentPitch * PITCH_MODIFIER * SENSITIVITY);
    }

    @Override
    public float roll() {
        return normalize(currentRoll * ROLL_MODIFIER * SENSITIVITY);
    }
}
