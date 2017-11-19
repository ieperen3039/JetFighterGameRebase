package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import org.joml.Vector2i;

import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_CENTER;

/**
 * @author Jorren
 */
public class MenuToggle extends MenuClickable {
    private String text;
    private boolean value;
    private String[] names;
    private Consumer<Boolean> handler;

    public MenuToggle(String text, int x, int y, int width, int height, String[] names, Consumer<Boolean> handler) {
        super(x, y, width, height);
        this.text = text;
        this.names = names;
        this.value = true;
        this.handler = handler;
    }

    public MenuToggle(String text, String[] names, Consumer<Boolean> handler) {
        this(text, 0, 0, names, handler);
    }

    public MenuToggle(String text, Consumer<Boolean> handler) {
        this(text, 0, 0, handler);
    }

    public MenuToggle(String text, int x, int y, Consumer<Boolean> handler) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, new String[]{"enabled","disabled"}, handler);
    }

    public MenuToggle(String text, int x, int y, String[] names, Consumer<Boolean> handler) {
        this(text, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, names, handler);
    }

    public MenuToggle(String text, Vector2i pos, String[] names, Consumer<Boolean> handler) {
        this(text, pos.x, pos.y, names, handler);
    }

    public MenuToggle(String text, Vector2i pos, Consumer<Boolean> handler) {
        this(text, pos.x, pos.y, handler);
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void draw(Hud hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.fill(MENU_FILL_COLOR);
        hud.stroke(MENU_STROKE_WIDTH, MENU_STROKE_COLOR);
        hud.text(x + width /2, (int) (y + TEXT_SIZE_LARGE + 10), TEXT_SIZE_LARGE, Hud.Font.MEDIUM, NVG_ALIGN_CENTER,
                String.format("%1$s: %2$s", text, names[value ? 0 : 1]), TEXT_COLOR);
    }

    @Override
    public void onClick(MouseTracker.MouseEvent event) {
        value = !value;
        handler.accept(value);
    }
}
