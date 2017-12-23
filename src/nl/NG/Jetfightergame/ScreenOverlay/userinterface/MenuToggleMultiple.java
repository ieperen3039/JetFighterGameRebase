package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Yoeri Poels
 */
public class MenuToggleMultiple extends MenuClickable {
    private String text;
    private int value;
    private String[] names;
    private Consumer<Integer> handler;

    public MenuToggleMultiple(String text, int width, int height, String[] names, Consumer<Integer> handler, int initial) {
        super(width, height);
        this.text = text;
        this.names = names;
        this.value = initial;
        this.handler = handler;
    }

    public MenuToggleMultiple(String text, String[] names, Consumer<Integer> handler) {
        this(text, MenuStyleSettings.BUTTON_WIDTH, MenuStyleSettings.BUTTON_HEIGHT, names, handler, 0);
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);
        hud.setFillColor(MenuStyleSettings.FILL_COLOR);
        hud.setStrokeColor(MenuStyleSettings.STROKE_COLOR);
        hud.text(x + width /2, (int) (y + MenuStyleSettings.TEXT_SIZE_LARGE + 10), MenuStyleSettings.TEXT_SIZE_LARGE, ScreenOverlay.Font.ORBITRON_MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$s", text, names[value]));
    }

    @Override
    public void onClick(int x, int y) {
        value = (value += 1)%names.length;

        handler.accept(value);
    }
}
