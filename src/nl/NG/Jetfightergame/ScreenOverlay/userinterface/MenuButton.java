package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Geert van Ieperen
 * a button with set shape and text
 */
public class MenuButton extends MenuClickable {

    private final String text;

    private Runnable click;

    public MenuButton(String text, Runnable click) {
        this(text, BUTTON_WIDTH, BUTTON_HEIGHT, click);
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
        hud.fill(MENU_FILL_COLOR);
        hud.stroke(MENU_STROKE_WIDTH, MENU_STROKE_COLOR);

        ScreenOverlay.Font font = ScreenOverlay.Font.MEDIUM;
        hud.text(x + width /2, y + TEXT_SIZE_LARGE + 10, TEXT_SIZE_LARGE, font, NVG_ALIGN_CENTER, text, TEXT_COLOR);
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
