package nl.NG.Jetfightergame.ScreenOverlay;

import org.joml.Vector2i;
import org.joml.Vector4f;

/**
 * Generic UI element that can be drawn in the hud.
 * @author Jorren Hendriks.
 */
public abstract class UIElement implements HudElement {

    public static final int INDENT = 10;
    public static final Vector4f MENU_FILL_COLOR = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
    public static final Vector4f MENU_STROKE_COLOR = new Vector4f(0.1f, 0.1f, 0.1f, 1.0f);
    public static final int MENU_STROKE_WIDTH = 5;
    public static final int TEXT_SIZE_LARGE = 48;
    public static final Vector4f TEXT_COLOR = new Vector4f();
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public UIElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Vector2i v) {
        return x <= v.x() && v.x() <= x + width &&
                y <= v.y() && v.y() <= y + height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
