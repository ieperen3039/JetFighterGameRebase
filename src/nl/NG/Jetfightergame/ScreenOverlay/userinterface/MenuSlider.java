package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Jorren
 */
public class MenuSlider extends MenuClickable {

    private String text;
    private float value;
    private Consumer<Float> handler;

    public MenuSlider(String text, int x, int y, int width, int height, Consumer<Float> handler) {
        super(x, y, width, height);
        this.text = text;
        this.value = 1f;
        this.handler = handler;
    }

    public MenuSlider(String text, int x, int y, Consumer<Float> handler) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, handler);
    }

    public MenuSlider(String text, Consumer<Float> handler) {
        this(text, 0, 0, handler);
    }

    @Override
    public void draw(Hud hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.fill(COLOR_BLUE);
        hud.stroke(STROKE_WIDTH, COLOR_PINK);
        hud.roundedRectangle(x + STROKE_WIDTH/2, y + STROKE_WIDTH/2,
                (int) ((width - STROKE_WIDTH) * Math.max(value, 0.05f)),height - STROKE_WIDTH, INDENT);
        hud.fill(COLOR_DARK);
        hud.text(x + width /2, (int) (y + TEXT_LARGE + 10), TEXT_LARGE, Hud.Font.MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$d%%", text, (int) ((value * 100) >= 1 ? value * 100 : 1)), COLOR_TEXT);
    }

    @Override
    public void onClick(MouseTracker.MouseEvent event) {
        value = Math.max((float) (event.getX() - this.x) / (float) this.width, 0f);
        handler.accept(value);
    }
}
