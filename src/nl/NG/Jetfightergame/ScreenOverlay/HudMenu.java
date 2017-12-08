package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.Controllers.TrackerClickListener;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuClickable;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuPositioner;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.MenuPositionerLeft;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jorren Hendriks
 * @author Geert van Ieperen
 */

public abstract class HudMenu implements TrackerClickListener {
    private final Hud hud;
    private UIElement[] activeElements;
    private boolean visible;
    private Runnable hudEntry;

    public HudMenu(Hud hud, boolean visible) {
        this.hud = hud;
        this.visible = visible;
        MouseTracker.getInstance().addClickListener(this, false);
    }

    /**
     * set the active elements to the defined elements
     * @param newElements new elements of the menu
     */
    public void switchContentTo(UIElement[] newElements) {
        activeElements = newElements;
        List<Runnable> entry = new LinkedList<>();


        // correct positions of buttons
        MenuPositioner caret = new MenuPositionerLeft();
        for (UIElement element : activeElements) {
            caret.place(element);
            entry.add(() -> element.draw(hud));
        }

        // destroy the current entries of the hud
        hud.destroy(hudEntry);
        // add the new list of elements to the hud, and receive new pointer
        hudEntry = () -> entry.forEach(Runnable::run);
        // only display it if it is currently visible
        if (visible) hud.create(hudEntry);
    }

    @Override
    public void clickEvent(int x, int y) {
        if (!visible) return;

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

    public void setVisibillity(boolean toVisible){
        visible = toVisible;
        if (toVisible) {
            hud.create(hudEntry);
        } else {
            hud.destroy(hudEntry);
        }
    }

    public UIElement[] getActiveElements() {
        return activeElements;
    }
}
