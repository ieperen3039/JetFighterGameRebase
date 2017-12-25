package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.*;
import static nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay.Font.ORBITRON_MEDIUM;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Geert van Ieperen
 * a button with set shape and text
 */
public class MenuButton extends MenuClickable {

    private final String text;

    private Runnable click;

    public MenuButton(String text, Runnable click) {
        this(text, MenuStyleSettings.BUTTON_WIDTH, MenuStyleSettings.BUTTON_HEIGHT, click);
    }

    /**
     * create a button that executes a click
     * @param text the text displayed on the button, will also be used to name in case of error
     * @param width
     * @param height
     * @param click
     */
    public MenuButton(String text, int width, int height, Runnable click) {
        super(width, height);
        this.text = text;
        this.click = click;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);

        hud.text(x + width /2, y + TEXT_SIZE_LARGE + INTERNAL_MARGIN,
                TEXT_SIZE_LARGE, ORBITRON_MEDIUM, NVG_ALIGN_CENTER, TEXT_COLOR,
                text
        );
    }

    @Override
    public void onClick(int x, int y) {
        try {
            click.run();
        } catch (Exception ex){
            throw new RuntimeException("Error occurred in button \"" + text + "\"", ex);
        }
    }
}
