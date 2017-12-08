package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 */
public class MenuPositionerCenter extends MenuPositioner {

    public MenuPositionerCenter(int screenWidth) {
        y = boundaryDistance;
        x = screenWidth / 2;

    }

    @Override
    public void place(UIElement element) {
        element.setPosition(x - element.getWidth() / 2, y);
        y += (margin + element.getHeight());
    }
}
