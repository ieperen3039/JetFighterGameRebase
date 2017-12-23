package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerClickListener;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuClickable;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuPositioner;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuPositionerLeft;

import java.util.Arrays;

/**
 * @author Jorren Hendriks
 * @author Geert van Ieperen
 */

public abstract class HudMenu implements TrackerClickListener {
    private final ScreenOverlay screenOverlay;
    private UIElement[] activeElements;

    public HudMenu(ScreenOverlay hud) {
        this.screenOverlay = hud;
        MouseTracker.getInstance().addClickListener(this, false);
    }

    /**
     * set the active elements to the defined elements
     * @param newElements new elements of the menu
     */
    public void switchContentTo(UIElement[] newElements) {
        activeElements = newElements;

        // destroy the current entries of the hud
        screenOverlay.removeMenuItem();

        // correct positions of buttons
        MenuPositioner caret = new MenuPositionerLeft();
        for (UIElement element : activeElements) {
            caret.place(element);
            screenOverlay.addMenuItem(element::draw);
        }

    }

    // note that these can only fire when mouse is not in capture mode
    @Override
    public void clickEvent(int x, int y) {
        Arrays.stream(activeElements)
                // take all clickable elements
                .filter(element -> element instanceof MenuClickable)
                // identify
                .map(element -> (MenuClickable) element)
                // take the button that is clicked
                .filter(button -> button.contains(x, y))
                // execute buttonpress
                .forEach(button -> button.onClick(x, y));
    }

    public UIElement[] getActiveElements() {
        return activeElements;
    }

    @Override
    public void cleanUp() {
        MouseTracker.getInstance().removeClickListener(this, false);
    }
}
