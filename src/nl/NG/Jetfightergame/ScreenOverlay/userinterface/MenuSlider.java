package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Jorren
 */
public class MenuSlider extends MenuClickable {

    private String text;
    private float value;
    private Consumer<Float> handler;

    public MenuSlider(String text, int width, int height, Consumer<Float> handler) {
        super(width, height);
        this.text = text;
        this.value = 1f;
        this.handler = handler;
    }

    public MenuSlider(String text, Consumer<Float> handler) {
        this(text, MenuStyleSettings.BUTTON_WIDTH, MenuStyleSettings.BUTTON_HEIGHT, handler);
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);
        hud.setFillColor(MenuStyleSettings.FILL_COLOR);
        hud.setStrokeColor(MenuStyleSettings.STROKE_COLOR);
        hud.roundedRectangle(x + MenuStyleSettings.STROKE_WIDTH /2, y + MenuStyleSettings.STROKE_WIDTH /2,
                (int) ((width - MenuStyleSettings.STROKE_WIDTH) * Math.max(value, 0.05f)),height - MenuStyleSettings.STROKE_WIDTH, MenuStyleSettings.INDENT);
        hud.setFillColor(MenuStyleSettings.COLOR_DARK);
        hud.text(x + width /2, (int) (y + MenuStyleSettings.TEXT_SIZE_LARGE + 10), MenuStyleSettings.TEXT_SIZE_LARGE, ScreenOverlay.Font.ORBITRON_MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$d%%", text, (int) ((value * 100) >= 1 ? value * 100 : 1)));
    }

    @Override
    public void onClick(int x, int y) {
        value = Math.max((float) (x - this.x) / (float) this.width, 0f);
        try {
            handler.accept(value);
        } catch (Exception ex){
            throw new RuntimeException("Error occurred in slider \"" + text + "\", with value" + value, ex);
        }
    }
}
