package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.ScreenOverlay.FontRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

import java.awt.*;
import java.util.function.Consumer;

/**
 * @author Jorren
 */
public class MenuToggle extends MenuClickable {
    private final Consumer<Boolean> click;
    private String text;
    private boolean value;
    private String[] names;

    public MenuToggle(String text, int x, int y, int width, int height, String[] names, Consumer<Boolean> handler) {
        super(x, y, width, height);
        this.text = text;
        this.names = names;
        this.value = true;
        this.click = handler;
    }

    public MenuToggle(String text, String[] names, Consumer<Boolean> handler) {
        this(text, 0, 0, names, handler);
    }

    public MenuToggle(String text, Consumer<Boolean> handler) {
        this(text, 0, 0, handler);
    }

    public MenuToggle(String text, int x, int y, Consumer<Boolean> handler) {
        this(text, x, y, MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT, new String[]{"enabled","disabled"}, handler);
    }

    public MenuToggle(String text, int x, int y, String[] names, Consumer<Boolean> handler) {
        this(text, x, y, MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT, names, handler);
    }

    public MenuToggle(String text, ScreenCoordinate pos, String[] names, Consumer<Boolean> handler) {
        this(text, pos.x, pos.y, names, handler);
    }

    public MenuToggle(String text, ScreenCoordinate pos, Consumer<Boolean> handler) {
        this(text, pos.x, pos.y, handler);
    }

    public void setValue(boolean value) {
        this.value = value;
    }


    private void draw(Hud.Painter hud) {
        Color backColor = hud.getFillColor();
        if (!value) hud.setFill(backColor.brighter());

        hud.roundedRectangle(x, y, width, height, MenuButton.INDENT);
        hud.setFill(backColor);
    }

    private void write(FontRenderer writer){
        String endText = String.format("%1$s: %2$s", text, names[value ? 0 : 1]);
        writer.setColor(MenuButton.COLOR_TEXT);
        writer.draw(endText, x + width /2, y + writer.getFontSize() + 10);

    }

    @Override
    public void onClick(int x, int y) {
        value = !value;
        click.accept(value);
    }

    @Override
    public void register(Hud hud) {
        hud.writer32pt.addItem(this::write);
        hud.painter.addItem(this::draw);
    }
}
