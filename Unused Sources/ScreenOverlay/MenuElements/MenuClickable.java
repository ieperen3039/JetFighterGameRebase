package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

/**
 * @author Jorren Hendriks.
 */
public abstract class MenuClickable extends MenuElement {

    public MenuClickable(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public boolean contains(ScreenCoordinate v) {
        int x = v.x;
        int y = v.y;
        return contains(x, y);
    }

    public boolean contains(int x, int y) {
        return this.x <= x && this.x + width >= x &&
                this.y <= y && this.y + height >= y;
    }

    /**
     * this method is fired when this button is clicked upon
     * @param x the x-coordinate of the mouse in screen-coordinates
     * @param y the y-coordinate in screen coordinates
     */
    public abstract void onClick(int x, int y);

}
