package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.Controllers.MouseTracker.MouseEvent;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import org.joml.Vector2i;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Geert van Ieperen
 * a button with set shape and text
 */
public class MenuButton extends MenuClickable {

    private final String text;

    private Consumer<MouseEvent> click;

    public MenuButton(String text, Consumer<MouseEvent> click) {
        this(text, 0, 0, click);
    }

    public MenuButton(String text, int x, int y, Consumer<MouseEvent> click) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, click);
    }

    public MenuButton(String text, int x, int y, int width, int height, Consumer<MouseEvent> click) {
        super(x, y, width, height);
        this.text = text;
        this.click = click;
    }

    public MenuButton(String text, Vector2i pos, Consumer<MouseEvent> click){
        this(text, pos.x, pos.y, click);
    }

    @Override
    public void draw(Hud hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.fill(COLOR_BLUE);
        hud.stroke(STROKE_WIDTH, COLOR_PINK);
        hud.text(x + width /2, (int) (y + TEXT_LARGE + 10), TEXT_LARGE, Hud.Font.MEDIUM, NVG_ALIGN_CENTER, text, COLOR_TEXT);
    }

    @Override
    public void onClick(MouseEvent event) {
        click.accept(event);
    }
}
