package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

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
    public void place(MenuElement element) {
        element.setPosition(x - element.getWidth() / 2, y);
        y += (margin + element.getHeight());
    }
}
