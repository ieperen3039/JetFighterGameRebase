package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.ScreenOverlay.HudElement;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 */
public abstract class MenuElement implements HudElement {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public MenuElement(int x, int y, int width, int height) {
        this.y = y;
        this.height = height;
        this.width = width;
        this.x = x;
    }

    public void setPosition(ScreenCoordinate position){
        setPosition(position.x, position.y);
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}
