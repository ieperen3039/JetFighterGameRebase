package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Assets.Powerups.PowerupType;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.*;

/**
 * @author Geert van Ieperen. Created on 18-7-2018.
 */
public class PowerupDisplay implements Consumer<ScreenOverlay.Painter> {
    private static final int MARGIN = 50;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 80;
    private final Player player;

    public PowerupDisplay(Player player) {
        this.player = player;
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        int xMax = hud.windowWidth - MARGIN;
        int yMax = hud.windowHeight - MARGIN;
        int xMin = xMax - WIDTH;
        int yMin = yMax - HEIGHT;

        hud.rectangle(
                xMin, yMin, WIDTH, HEIGHT,
                Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR
        );

        PowerupType power = player.jet().getCurrentPowerup();
        hud.text(
                xMin + MARGIN / 2, yMin + MARGIN / 2, TEXT_SIZE,
                FONT, NanoVG.NVG_ALIGN_LEFT, HUD_COLOR,
                (power == null) ? "-" : power.toString()
        );
    }
}
