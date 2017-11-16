package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;


import nl.NG.Jetfightergame.ScreenOverlay.FontRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

/**
 * @author Geert van Ieperen
 * a field with the make-up of a menubutton, automatically including title and back-button
 */
public class MenuTextField extends MenuElement {

    public static final int MARGIN = 5;

    private final String title;
    private final String[] content;

    public MenuTextField(String title, String[] content, int width, int height) {
        this (title, content, 0, 0, width, height);
    }

    /**
     * Creates a textfield
     * @param title The title of the field
     * @param content the content this textbox has to display
     */
    public MenuTextField(String title, String[] content, int x, int y, int width, int heigth) {
        super(x, y, width, heigth);
        this.title = title;
        this.content = content;
    }

    @Override
    public void register(Hud hud){
        hud.writer32pt.addItem(this::write);
        hud.writer18pt.addItem(this::writeSmall);
        hud.painter.addItem(this::draw);
    }

    private void draw(Hud.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuButton.INDENT);
    }

    private void write(FontRenderer writer) {
        writer.setColor(MenuButton.COLOR_TEXT);
        writer.draw(title, x + width /2, y + MARGIN);
    }

    private void writeSmall(FontRenderer writer){
        writer.setColor(MenuButton.COLOR_TEXT);
        int x = MenuPositioner.STD_BOUND_DIST;
        for (String line : content) {
            writer.draw(line, x, y);
            x += (MenuPositioner.STD_MARGIN + writer.getFontSize());
        }
    }

    public boolean contains(ScreenCoordinate v) {
        return x <= v.x && v.x <= x + width &&
                y <= v.y && v.y <= y + height;
    }
}
