package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.*;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

/**
 * @author Geert van Ieperen
 * a field with the make-up of a menubutton, automatically including title and back-button
 */
public class MenuTextField extends UIElement {

    private final String title;
    private final String[] content;

    /**
     * Creates a textfield
     * @param title The title of the field
     * @param content the content this textbox has to display
     */
    public MenuTextField(String title, String[] content, int width) {
        super(width, (TEXT_SIZE_SMALL + INTERTEXT_MARGIN) * content.length + TEXT_SIZE_LARGE + INTERTEXT_MARGIN + 2* MARGIN);

        this.title = title;
        this.content = content;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);

        final int middle = this.x + width / 2;
        hud.text(middle, y + MARGIN, TEXT_SIZE_LARGE, ScreenOverlay.Font.ORBITRON_MEDIUM,
                NVG_ALIGN_CENTER | NVG_ALIGN_TOP, title);

        int textYPosition = y + MARGIN*2 + TEXT_SIZE_LARGE;
        for (String line : content) {
            hud.text(middle, textYPosition, TEXT_SIZE_SMALL, ScreenOverlay.Font.ORBITRON_MEDIUM, NVG_ALIGN_CENTER | NVG_ALIGN_TOP, line);
            textYPosition += TEXT_SIZE_SMALL + INTERTEXT_MARGIN;
        }
    }
}
