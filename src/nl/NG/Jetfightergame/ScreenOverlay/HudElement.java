package nl.NG.Jetfightergame.ScreenOverlay;

import org.joml.Vector2i;

/**
 * Interface for objects that can be drawn on a Hud.
 * @author Jorren
 */
public interface HudElement {

    /**
     * Draw this hud element.
     *
     * @param hud The hud on which to drawObjects this element.
     */
    void draw(ScreenOverlay.Painter hud);

    void setPosition(Vector2i position);

    void setPosition(int x, int y);
}
