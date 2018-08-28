package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.KeyBinding;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_COLOR;
import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_STROKE_WIDTH;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * considers the position of the mouse to be relative to the middle (initial position)
 * Not moving the mouse will result in a continuous roll/pitch of the plane
 */
public class PassivePCControllerRelative extends PassivePCController {
    private static final float SENSITIVITY = 0.2f;
    private final int INDICATOR_SENSITIVITY = 3;

    @Override
    public Consumer<ScreenOverlay.Painter> hudElement() {
        return (hud) -> {
            final int xPos = (mouseX / INDICATOR_SENSITIVITY) + (hud.windowWidth / 2);
            final int yPos = -(mouseY / INDICATOR_SENSITIVITY) + (hud.windowHeight / 2); // minus for (mouse -> screen)
            hud.circle(xPos, yPos, 30, Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR);
            hud.line(HUD_STROKE_WIDTH, Color4f.BLACK, hud.windowWidth/2, hud.windowHeight/2, xPos, yPos);
        };
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
    }

    @Override
    protected float getKeyAxis(KeyBinding keyUp, KeyBinding keyDown) {
        int i = 0;
        if (keyboard.isPressed(keyUp.getKey())) i++;
        if (keyboard.isPressed(keyDown.getKey())) i--;
        return (i);
    }

    @Override
    protected float getMouseY(float modifier) {
        return normalize(mouseY * modifier * SENSITIVITY);
    }

    @Override
    protected float getMouseX(float modifier) {
        return normalize(mouseX * modifier * SENSITIVITY);
    }
}
