package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

/**
 * @author Jorren Hendriks.
 */
public abstract class MenuClickable extends UIElement {

    public MenuClickable(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public abstract void onClick(MouseTracker.MouseEvent event);
}
