package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.JFGFont.LUCIDA_CONSOLE;

/**
 * @author Geert van Ieperen. Created on 14-8-2018.
 */
public class CountDownTimer implements Consumer<ScreenOverlay.Painter> {
    public static final int BOX_SIZE = 50;
    private static final int PIXELS_DISPLACE = 100;
    private final GameTimer timer;

    private float timeOfZero;

    public CountDownTimer(float timeOfZero, GameTimer timer) {
        this.timeOfZero = timeOfZero;
        this.timer = timer;
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        float timeRemaining = timeOfZero - timer.time();
        if (timeRemaining < 0) return;

        int xCoord = hud.windowWidth / 2;
        int yCoord = hud.windowHeight / 4;

        int integerRemaining = (int) timeRemaining;
        hud.text(
                xCoord, yCoord + BOX_SIZE / 2, 64, LUCIDA_CONSOLE,
                NanoVG.NVG_ALIGN_CENTER, Color4f.GREEN,
                Integer.toString(integerRemaining + 1)
        );

        int exp = (int) ((timeRemaining - integerRemaining) * PIXELS_DISPLACE);
        hud.line(
                HUDStyleSettings.HUD_STROKE_WIDTH, HUDStyleSettings.HUD_COLOR,
                xCoord - BOX_SIZE, yCoord - exp - BOX_SIZE,
                xCoord + BOX_SIZE, yCoord - exp - BOX_SIZE
        );
        hud.line(
                HUDStyleSettings.HUD_STROKE_WIDTH, HUDStyleSettings.HUD_COLOR,
                xCoord - exp - BOX_SIZE, yCoord - BOX_SIZE,
                xCoord - exp - BOX_SIZE, yCoord + BOX_SIZE
        );
        hud.line(
                HUDStyleSettings.HUD_STROKE_WIDTH, HUDStyleSettings.HUD_COLOR,
                xCoord + exp + BOX_SIZE, yCoord + BOX_SIZE,
                xCoord + exp + BOX_SIZE, yCoord - BOX_SIZE
        );
        hud.line(
                HUDStyleSettings.HUD_STROKE_WIDTH, HUDStyleSettings.HUD_COLOR,
                xCoord + BOX_SIZE, yCoord + exp + BOX_SIZE,
                xCoord - BOX_SIZE, yCoord + exp + BOX_SIZE
        );
    }

    public void setTime(float timeOfZero) {
        this.timeOfZero = timeOfZero;
    }
}
