package nl.NG.Jetfightergame.ScreenOverlay;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 10-11-2017.
 */
public interface FontRenderer {
    void draw(String text, int xPos, int yPos);

    void setColor(Color newColor);

    void beginRendering(GL2 gl, int screenWidth, int screenHeight);

    void endRendering();

    int getFontSize();
}
