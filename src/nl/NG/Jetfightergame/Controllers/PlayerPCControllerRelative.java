package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_COLOR;
import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_STROKE_WIDTH;
import static nl.NG.Jetfightergame.Settings.PITCH_MODIFIER;
import static nl.NG.Jetfightergame.Settings.ROLL_MODIFIER;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * considers the position of the mouse to be relative to the middle (initial position)
 * Not moving the mouse will result in a continuous roll/pitch of the plane
 */
public class PlayerPCControllerRelative extends PlayerPCController {
    private static final float SENSITIVITY = 0.2f;
    private final Consumer<ScreenOverlay.Painter> moveVector;
    private final int INDICATOR_SENSITIVITY = 3;

    public PlayerPCControllerRelative() {
        moveVector = (hud) -> {
            final int xPos = (currentRoll / INDICATOR_SENSITIVITY) + (hud.windowWidth / 2);
            final int yPos = (currentPitch / INDICATOR_SENSITIVITY) + (hud.windowHeight / 2);
            hud.circle(xPos, yPos, 30, Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR);
            hud.line(HUD_STROKE_WIDTH, Color4f.BLACK, hud.windowWidth/2, hud.windowHeight/2, xPos, yPos);
        };

        ScreenOverlay.addHudItem(moveVector);
    }

    @Override
    public float pitch() {
        return normalize(currentPitch * PITCH_MODIFIER * SENSITIVITY * -1);
    }

    @Override
    public float roll() {
        return normalize(currentRoll * ROLL_MODIFIER * SENSITIVITY);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        ScreenOverlay.removeHudItem(moveVector);
    }
}
