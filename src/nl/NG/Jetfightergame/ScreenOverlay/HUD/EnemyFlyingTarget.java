package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector2i;

import java.util.function.Supplier;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.*;
import static nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay.Font.LUCIDA_CONSOLE;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public class EnemyFlyingTarget extends MovingTarget {

    /**
     * create a targeting wrapper for the HUD to target entities.
     *
     * @param subject the entity aimed at
     * @param cameraPosition  the position of the camera
     */
    public EnemyFlyingTarget(MovingEntity subject, Supplier<PosVector> cameraPosition) {
        super(subject, cameraPosition);
    }

    @Override
    protected void draw(ScreenOverlay.Painter hud) {
        Vector2i pos = entityPosition(hud);
        if (pos == null) return;

        int size = iconSize();
        final int halfSize = size / 2;
        hud.rectangle(pos.x() - halfSize, pos.y() - halfSize, size, size, Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR);

        int x = pos.x() + halfSize + ICON_TEXT_DISPLACE;
        int y = pos.y() - halfSize;
        pos = new Vector2i(x, y);

        write(hud, pos, "TGT", Color4f.RED);
        write(hud, pos, targetName(), HUD_COLOR);
        write(hud, pos, distance(), HUD_COLOR);
    }

    private static void write(ScreenOverlay.Painter hud, Vector2i pos, Object text, Color4f color) {
        hud.text(pos.x(), pos.y(),
                TEXT_SIZE, LUCIDA_CONSOLE,
                NVG_ALIGN_LEFT, color,
                text.toString()
        );
        pos.add(0, TEXT_SIZE + ICON_TEXT_DISPLACE);
    }
}
