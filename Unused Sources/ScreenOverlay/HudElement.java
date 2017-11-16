package nl.NG.Jetfightergame.ScreenOverlay;

/**
 * Interface for objects that can be drawn on a Hud.
 * @author Jorren
 */
public interface HudElement {

    void register(Hud hud);

    int getHeight();

    int getWidth();
}
