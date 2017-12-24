package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Vectors.Color4f;

/**
 * @author Geert van Ieperen
 * created on 23-12-2017.
 */
public class MenuStyleSettings {
    public static final int INDENT = 15;
    public static final Color4f MENU_FILL_COLOR = Color4f.TRANSPARENT_GREY;
    public static final Color4f MENU_STROKE_COLOR = new Color4f(0.1f, 0.1f, 0.1f, 1.0f);
    public static final Color4f COLOR_DARK = MENU_FILL_COLOR.darken(0.4f);
    public static final int MENU_STROKE_WIDTH = 8;
    public static final int TEXT_SIZE_LARGE = 48;
    public static final Color4f TEXT_COLOR = Color4f.WHITE;
    public static final int BUTTON_WIDTH = 600;
    public static final int BUTTON_HEIGHT = 80;
}
