package nl.NG.Jetfightergame.ScreenOverlay;

/**
 * Interface for objects that can be drawn on a Hud.
 * @author Jorren
 */
public interface HudElement {

    /**
     * Draw this hud element.
     *
     * @param hud The hud on which to draw this element.
     */
    void draw(Hud hud);

}
