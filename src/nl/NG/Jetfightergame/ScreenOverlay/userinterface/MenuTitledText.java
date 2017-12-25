package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.TEXT_SIZE_LARGE;
import static nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuTextField.TEXT_DIFF;

/**
 * @author Geert van Ieperen
 * created on 23-12-2017.
 */
public class MenuTitledText extends UIElement {

    private MenuTextField title;
    private MenuTextField content;

    public MenuTitledText(String title, String[] content, int width) {
        super(width, (MenuStyleSettings.TEXT_SIZE_SMALL + TEXT_DIFF) * content.length + 2* MenuStyleSettings.EXTERNAL_MARGIN + TEXT_SIZE_LARGE + 2* TEXT_DIFF);
        this.title = new MenuTextField(new String[]{title}, width, TEXT_SIZE_LARGE);
        this.content = new MenuTextField(content, width, MenuStyleSettings.TEXT_SIZE_SMALL);
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
        return getY() + TEXT_SIZE_LARGE + MenuStyleSettings.EXTERNAL_MARGIN + 2* TEXT_DIFF;
    }
}
