package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.ScreenOverlay.FontRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;

import java.awt.*;
import java.util.function.Consumer;

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
    public void register(Hud hud) {
        hud.painter.addItem(this::draw);
        hud.writer32pt.addItem(this::write);
    }

    private void draw(Hud.Painter hud) {
        hud.roundedRectangle(x, y, width, height, INDENT);
        hud.setFill(Color.BLUE);
        hud.setStroke(STROKE_WIDTH, STROKE_COLOR);
        hud.roundedRectangle(x + STROKE_WIDTH/2, y + STROKE_WIDTH/2,
                (int) ((width - STROKE_WIDTH) * Math.max(value, 0.05f)),height - STROKE_WIDTH, INDENT);
        hud.setFill(FILL_COLOR);
    }

    private void write(FontRenderer writer) {
        String text1 = String.format("%1$s: %2$d%%", text, (int) ((value * 100) >= 1 ? value * 100 : 1));
        writer.setColor(COLOR_TEXT);
        writer.draw(text1, x + width /2, y + writer.getFontSize() + 10);
    }

    @Override
    public void onClick(int x, int y) {
        value = Math.max((float) (x - this.x) / (float) this.width, 0f);
        handler.accept(value);
    }

}
