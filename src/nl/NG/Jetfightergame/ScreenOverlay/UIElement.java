package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Vectors.Color4f;
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
    public static final Vector4f TEXT_COLOR = Color4f.WHITE.toVector4f();
    protected int x = 0;
    protected int y = 0;
    protected int width;
    protected int height;

    public UIElement(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean contains(Vector2i v) {
        return contains(v.x, v.y);
    }

    public boolean contains(int x, int y) {
        return this.x <= x && x <= this.x + width &&
                this.y <= y && y <= this.y + height;
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

    public Vector2i getPosition(){
        return new Vector2i(x, y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void setPosition(Vector2i position) {
        this.x = position.x;
        this.y = position.y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
