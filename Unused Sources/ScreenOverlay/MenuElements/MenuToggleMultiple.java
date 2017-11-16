package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.ScreenOverlay.FontRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

import java.util.function.Consumer;

/**
 * @author Yoeri Poels
 */
public class MenuToggleMultiple extends MenuClickable {
    private String text;
    private int value;
    private String[] names;
    private Consumer<Integer> handler;

    public MenuToggleMultiple(String text, int x, int y, int width, int height, String[] names, Consumer<Integer> handler) {
        super(x, y, width, height);
        this.text = text;
        this.names = names;
        this.value = 0;
        this.handler = handler;
    }

    public MenuToggleMultiple(String text, String[] names, Consumer<Integer> handler) {
        this(text, 0, 0, names, handler);
    }

    public MenuToggleMultiple(String text, int x, int y, String[] names, Consumer<Integer> handler) {
        this(text, x, y, MenuButton.BUTTON_WIDTH, MenuButton.BUTTON_HEIGHT, names, handler);
    }

    public MenuToggleMultiple(String text, ScreenCoordinate pos, String[] names, Consumer<Integer> handler) {
        this(text, pos.x, pos.y, names, handler);
    }


    public void setValue(int value) {
        this.value = value;
    }


    private void draw(Hud.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuButton.INDENT);
    }

    private void write(FontRenderer writer){
        writer.setColor(MenuButton.COLOR_TEXT);
        String endText = String.format("%1$s: %2$s", text, names[value]);
        writer.draw(endText, x + width /2, y + writer.getFontSize() + 10);
    }

    @Override
    public void onClick(int x, int y) {
        value = (value += 1) % names.length;

        handler.accept(value);
    }

    @Override
    public void register(Hud hud) {
        hud.painter.addItem(this::draw);
        hud.writer32pt.addItem(this::write);
    }
}
