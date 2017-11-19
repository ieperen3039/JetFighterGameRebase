package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import org.joml.Vector4f;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Jorren
 */
public class MenuSlider extends MenuClickable {

    public static final Vector4f COLOR_DARK = MENU_FILL_COLOR.mul(0.6f);
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
        hud.fill(MENU_FILL_COLOR);
        hud.stroke(MENU_STROKE_WIDTH, MENU_STROKE_COLOR);
        hud.roundedRectangle(x + MENU_STROKE_WIDTH/2, y + MENU_STROKE_WIDTH/2,
                (int) ((width - MENU_STROKE_WIDTH) * Math.max(value, 0.05f)),height - MENU_STROKE_WIDTH, INDENT);
        hud.fill(COLOR_DARK);
        hud.text(x + width /2, (int) (y + TEXT_SIZE_LARGE + 10), TEXT_SIZE_LARGE, Hud.Font.MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$d%%", text, (int) ((value * 100) >= 1 ? value * 100 : 1)), TEXT_COLOR);
    }

    @Override
    public void onClick(MouseTracker.MouseEvent event) {
        value = Math.max((float) (event.getX() - this.x) / (float) this.width, 0f);
        handler.accept(value);
    }
}
