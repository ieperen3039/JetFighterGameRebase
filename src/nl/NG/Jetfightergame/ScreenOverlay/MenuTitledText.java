package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuTextField;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.TEXT_SIZE_LARGE;
import static nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuTextField.*;

/**
 * @author Geert van Ieperen
 * created on 23-12-2017.
 */
public class MenuTitledText extends UIElement {

    private MenuTextField title;
    private MenuTextField content;

    public MenuTitledText(String title, String[] content, int width) {
        super(width, (TEXT_SIZE_SMALL + INTERTEXT_MARGIN) * content.length + 2*MARGIN + TEXT_SIZE_LARGE + 2*INTERTEXT_MARGIN);
        this.title = new MenuTextField(new String[]{title}, width, TEXT_SIZE_LARGE);
        this.content = new MenuTextField(content, width, TEXT_SIZE_SMALL);


    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        title.setPosition(x, y);
        content.setPosition(x, contentY());
    }

    @Override
    public void draw(ScreenOverlay.Painter hud) {
        title.draw(hud);
        content.draw(hud);
    }

    private int contentY() {
        return getY() + TEXT_SIZE_LARGE + MARGIN + 2*INTERTEXT_MARGIN;
    }
}
