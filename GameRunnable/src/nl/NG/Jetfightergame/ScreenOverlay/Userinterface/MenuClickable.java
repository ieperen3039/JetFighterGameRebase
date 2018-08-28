package nl.NG.Jetfightergame.ScreenOverlay.Userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

/**
 * @author Jorren Hendriks.
 */
public abstract class MenuClickable extends UIElement {

    public MenuClickable(int width, int height) {
        super(width, height);
    }

    /**
     * is called when this element is clicked on. coordinates are screen coordinates, NOT relative
     * @param x screen x coordinate
     * @param y screen y coordinate
     */
    public abstract void onClick(int x, int y);
}
