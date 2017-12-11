package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import org.joml.Vector2i;

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

    public MenuToggleMultiple(String text, int x, int y, int width, int height, String[] names, Consumer<Integer> handler) {
        super(width, height);
        this.text = text;
        this.names = names;
        this.value = 0;
        this.handler = handler;
    }

    public MenuToggleMultiple(String text, String[] names, Consumer<Integer> handler) {
        this(text, 0, 0, names, handler);
    }

    public MenuToggleMultiple(String text, int x, int y, String[] names, Consumer<Integer> handler) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, names, handler);
    }

    public MenuToggleMultiple(String text, Vector2i pos, String[] names, Consumer<Integer> handler) {
        this(text, pos.x, pos.y, names, handler);
    }


    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.fill(MENU_FILL_COLOR);
        hud.stroke(MENU_STROKE_WIDTH, MENU_STROKE_COLOR);
        hud.text(x + width /2, (int) (y + TEXT_SIZE_LARGE + 10), TEXT_SIZE_LARGE, ScreenOverlay.Font.MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$s", text, names[value]), TEXT_COLOR);
    }

    @Override
    public void onClick(int x, int y) {
        value = (value += 1)%names.length;

        handler.accept(value);
    }
}
