package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Tools.AutomatedCollection;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 */
public class AutoFont extends AutomatedCollection<FontRenderer> {

    public static final AutoFont arial12pt = new AutoFont(new Font("Arial", Font.PLAIN, 12));
    public static final AutoFont SansSherif18 = new AutoFont(new Font("SansSerif", Font.PLAIN, 18));
    public static final AutoFont SansSherif32 = new AutoFont(new Font("SansSerif", Font.PLAIN, 32));
    public static final AutoFont SansSherif48 = new AutoFont(new Font("SansSerif", Font.PLAIN, 48));

    // refers to external frame
    private static int frameWidth, frameHeight;

    public AutoFont(Font font) {
        super(new TextRendererAdapter(font));
        clear();
    }

    public static void setFrameDimensions(int width, int height){
        frameWidth = width;
        frameHeight = height;
    }

    public static void text(int x, int y, Color color, String text, FontRenderer writer){
        writer.setColor(color);
        writer.draw(text, x, y);
    }

    /**
     * create a static textfield that is no longer dependent on the calling method
     */
    public void addDrawable(int x, int y, String text, Color color){
        addItem(r -> {
            executer.setColor(color);
            executer.draw(text, x, y);
        });
    }

    @Override
    public void drawItems(GL2 gl){
        executer.beginRendering(gl, frameWidth, frameHeight);
        execute();
        executer.endRendering();
    }
}
