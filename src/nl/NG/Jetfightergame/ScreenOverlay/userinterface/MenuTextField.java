package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;
import org.joml.Vector2i;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

/**
 * @author Geert van Ieperen
 * a field with the make-up of a menubutton, automatically including title and back-button
 */
public class MenuTextField extends UIElement {

    public static final double MARGIN = 20; //TODO adapt with textsize?
    public static final int TEXT_SIZE_SMALL = 30;
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
    public void draw(Hud hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.fill(MENU_FILL_COLOR);
        hud.stroke(MENU_STROKE_WIDTH, MENU_STROKE_COLOR);

        hud.text(x + width /2, (int) (y + MARGIN), TEXT_SIZE_LARGE, Hud.Font.MEDIUM,
                NVG_ALIGN_CENTER | NVG_ALIGN_TOP, title, TEXT_COLOR);

        MenuPositioner pos = new MenuPositioner(x + width/2, (int) (y + TEXT_SIZE_LARGE + 2* MARGIN), 6);
        for (String line : content) {
            Vector2i p = pos.place(0, TEXT_SIZE_SMALL, true);
            hud.text(p.x, p.y, TEXT_SIZE_SMALL, Hud.Font.MEDIUM, NVG_ALIGN_CENTER | NVG_ALIGN_TOP, line, TEXT_COLOR);
        }
    }
}
