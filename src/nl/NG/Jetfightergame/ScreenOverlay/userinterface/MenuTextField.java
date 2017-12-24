package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.TEXT_COLOR;
import static nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay.Font.ORBITRON_MEDIUM;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_TOP;

/**
 * @author Geert van Ieperen
 * a field with the make-up of a menubutton, automatically including title and back-button
 */
public class MenuTextField extends UIElement {

    public static final int MARGIN = 20; //TODO adapt with textsize?
    public static final int TEXT_SIZE_SMALL = 30;
    public static final int INTERTEXT_MARGIN = MenuPositioner.STD_MARGIN/2;
    private final String[] content;
    private int textSize;

    /**
     * Creates a textfield
     * @param content the content this textbox has to display
     * @param textSize in pt
     */
    public MenuTextField(String[] content, int width, int textSize) {
        super(width, (textSize + INTERTEXT_MARGIN) * content.length + MARGIN);

        this.content = content;
        this.textSize = textSize;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);

        final int middle = this.x + width / 2;

        int textYPosition = y + INTERTEXT_MARGIN;
        for (String line : content) {
            hud.text(middle, textYPosition, textSize, ORBITRON_MEDIUM, NVG_ALIGN_CENTER | NVG_ALIGN_TOP, TEXT_COLOR, line);
            textYPosition += textSize + INTERTEXT_MARGIN;
        }
    }
}
