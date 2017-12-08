package nl.NG.Jetfightergame.ScreenOverlay.userinterface;

import nl.NG.Jetfightergame.ScreenOverlay.UIElement;

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
    public void place(UIElement element){
        element.setPosition(x, y);
        y += (margin + element.getHeight());
    }
}
