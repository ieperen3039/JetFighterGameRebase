package nl.NG.Jetfightergame.ScreenOverlay.Userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;
import nl.NG.Jetfightergame.Settings.MenuStyleSettings;

import static nl.NG.Jetfightergame.ScreenOverlay.JFGFonts.ORBITRON_MEDIUM;
import static nl.NG.Jetfightergame.Settings.MenuStyleSettings.*;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Geert van Ieperen
 * a field with the make-up of a menubutton, automatically including title and back-button
 */
public class MenuTextField extends UIElement {

    public static final int TEXT_DIFF = EXTERNAL_MARGIN /2;
    private final String[] content;
    private int textSize;

    /**
     * Creates a textfield
     * @param content the content this textbox has to display
     * @param textSize in pt
     */
    public MenuTextField(String[] content, int width, int textSize) {
        super(width, (textSize + TEXT_DIFF) * content.length + 2*INTERNAL_MARGIN);
        this.content = content.clone();
        this.textSize = textSize;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);

        final int middle = this.x + width / 2;

        int textYPosition = y + textSize + INTERNAL_MARGIN;
        for (String line : content) {
            hud.text(middle, textYPosition, textSize, ORBITRON_MEDIUM, NVG_ALIGN_CENTER, TEXT_COLOR, line);
            textYPosition += textSize + TEXT_DIFF;
        }
    }
}
