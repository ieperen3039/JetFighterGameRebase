package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

/**
 * @author Geert van Ieperen
 *         created on 9-11-2017.
 */
public class MenuPositionerLeft extends MenuPositioner {

    public MenuPositionerLeft() {
        y = boundaryDistance;
        x = boundaryDistance;
    }

    @Override
    public void place(MenuElement element){
        element.setPosition(x, y);
        y += (margin + element.getHeight());
    }
}
