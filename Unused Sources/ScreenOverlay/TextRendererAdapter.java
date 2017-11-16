package nl.NG.Jetfightergame.ScreenOverlay;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 10-11-2017.
 */
public class TextRendererAdapter implements FontRenderer {
    private final TextRenderer instance;

    public TextRendererAdapter(Font font) {
        this.instance = new TextRenderer(font);
    }

    @Override
    public void draw(String text, int xPos, int yPos) {
        instance.draw(text, xPos, yPos);
    }

    @Override
    public void setColor(Color newColor) {
        instance.setColor(newColor);
    }

    @Override
    public void beginRendering(GL2 gl, int screenWidth, int screenHeight) {
        instance.beginRendering(screenWidth, screenHeight, false);
    }

    @Override
    public void endRendering() {
        instance.flush();
        instance.endRendering();
    }

    @Override
    public int getFontSize() {
        return instance.getFont().getSize();
    }
}
