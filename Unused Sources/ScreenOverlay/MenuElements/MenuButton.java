package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.ScreenOverlay.FontRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

import java.awt.*;

/**
 * @author Geert van Ieperen
 * a button with set shape and text
 */
public class MenuButton extends MenuClickable {

    public static final int BUTTON_WIDTH = 300;
    public static final int BUTTON_HEIGHT = 100;
    public static final int INDENT = 5;
    public static final int STROKE_WIDTH = 2;
    public static final Color STROKE_COLOR = Color.DARK_GRAY;
    public static final Color COLOR_TEXT = Color.WHITE;
    public static final Color FILL_COLOR = Color.GRAY;

    private final String text;
    private final Runnable action;

    public MenuButton(String text, Runnable action) {
        this(text, 0, 0, action);
    }

    public MenuButton(String text, int x, int y, Runnable action) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, action);
    }

    public MenuButton(String text, ScreenCoordinate pos, Runnable action){
        this(text, pos.x, pos.y, action);
    }

    public MenuButton(String text, int x, int y, int width, int height, Runnable action) {
        super(x, y, width, height);
        this.text = text;
        this.action = action;
    }

    @Override
    public void register(Hud hud) {
        hud.painter.addItem(this::draw);
        hud.writer32pt.addItem(this::write);
    }

    private void draw(Hud.Painter hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
    }

    private void write(FontRenderer writer){
        writer.setColor(COLOR_TEXT);
        writer.draw(text, x + width /2, (y + (height - writer.getFontSize()/2)));
    }

    @Override
    public void onClick(int x, int y) {
        action.run();
    }

}
