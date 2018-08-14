package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.Collection;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.*;

/**
 * @author Geert van Ieperen. Created on 18-7-2018.
 */
public class PowerupDisplay implements Consumer<ScreenOverlay.Painter> {
    private static final int MARGIN = 50;
    private static final int WIDTH = 240;
    private static final int HEIGHT = 50;
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

        PowerupType power = player.jet().getCurrentPowerup();

        Collection<PowerupColor> res = power.getRequiredResources();
        int recWidth = WIDTH / res.size();
        int i = 0;
        for (PowerupColor resource : res) {
            hud.rectangle(
                    xMin + (recWidth * i++), yMin, recWidth, HEIGHT,
                    resource.color, 0, Color4f.INVISIBLE
            );
        }

        hud.rectangle(
                xMin, yMin, WIDTH, HEIGHT,
                Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR
        );

        hud.text(
                xMin + WIDTH / 2, yMin + MARGIN, TEXT_SIZE,
                FONT, NanoVG.NVG_ALIGN_CENTER, Color4f.BLACK,
                (power == PowerupType.NONE) ? "" : power.toString()
        );
    }
}
