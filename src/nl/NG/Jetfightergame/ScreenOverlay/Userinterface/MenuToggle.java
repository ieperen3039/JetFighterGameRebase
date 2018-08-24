package nl.NG.Jetfightergame.ScreenOverlay.Userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.MenuStyleSettings;
import org.joml.Vector2i;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.JFGFont.ORBITRON_MEDIUM;
import static nl.NG.Jetfightergame.Settings.MenuStyleSettings.TEXT_COLOR;
import static nl.NG.Jetfightergame.Settings.MenuStyleSettings.TEXT_SIZE_LARGE;
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
        super(width, height);
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
        this(text, x, y, MenuStyleSettings.BUTTON_WIDTH, MenuStyleSettings.BUTTON_HEIGHT, new String[]{"enabled","disabled"}, handler);
    }

    public MenuToggle(String text, int x, int y, String[] names, Consumer<Boolean> handler) {
        this(text, x, y, MenuStyleSettings.BUTTON_WIDTH, MenuStyleSettings.BUTTON_HEIGHT, names, handler);
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
    public void draw(ScreenOverlay.Painter hud) {
        hud.roundedRectangle(x, y, width, height, MenuStyleSettings.INDENT);
        hud.text(x + width /2, y + TEXT_SIZE_LARGE + 10,
                TEXT_SIZE_LARGE, ORBITRON_MEDIUM, NVG_ALIGN_CENTER,
                TEXT_COLOR, String.format("%1$s: %2$s", text, names[value ? 0 : 1]));
    }

    @Override
    public void onClick(int x, int y) {
        value = !value;
        handler.accept(value);
    }
}
