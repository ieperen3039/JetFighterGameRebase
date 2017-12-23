package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuPositioner;
import nl.NG.Jetfightergame.Vectors.Color4f;

/**
 * @author Geert van Ieperen
 * created on 23-12-2017.
 */
public class MenuStyleSettings {
    public static final int INDENT = 15;
    public static final Color4f FILL_COLOR = new Color4f(0.4f, 0.4f, 0.4f, 0.5f);
    public static final Color4f STROKE_COLOR = new Color4f(0.1f, 0.1f, 0.1f, 1.0f);
    public static final Color4f COLOR_DARK = FILL_COLOR.darken(0.5f);
    public static final int STROKE_WIDTH = 8;
    public static final int TEXT_SIZE_LARGE = 48;
    public static final Color4f TEXT_COLOR = Color4f.WHITE;
    public static final int BUTTON_WIDTH = 600;
    public static final int BUTTON_HEIGHT = 80;
    public static final int MARGIN = 20; //TODO adapt with textsize?
    public static final int TEXT_SIZE_SMALL = 30;
    public static final int INTERTEXT_MARGIN = MenuPositioner.STD_MARGIN;
}
