package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.Controllers.TrackerClickListener;
import nl.NG.Jetfightergame.GameObjects.Drawable;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.ScreenOverlay.HudElement;

import java.util.function.BooleanSupplier;

/**
 * @author Geert van Ieperen, Jorren Hendriks
 */

public abstract class HudMenu implements TrackerClickListener, Drawable {
    private final Hud hud;
    private final BooleanSupplier shouldBeVisible;
    private MenuElement[] activeElements;

    private boolean visible;

    public HudMenu(Hud hud, BooleanSupplier shouldBeVisible) {
        this.hud = hud;
        this.shouldBeVisible = shouldBeVisible;
        visible = shouldBeVisible.getAsBoolean();
        MouseTracker.getInstance().addClickListener(this, false);
    }

    /**
     * set the active elements to the defined elements
     * @param newElements
     */
    public void switchContentTo(MenuElement[] newElements) {
        activeElements = newElements;
        hud.clear();

        // correct positions of buttons
        MenuPositionerLeft caret = new MenuPositionerLeft();
        for (MenuElement element : activeElements) {
            caret.place(element);
        }

        for (MenuElement elt : newElements){
            elt.register(hud);
        }
    }

    @Override
    public void draw(GL2 gl) {
        if (visible){
            if (!shouldBeVisible.getAsBoolean()) {
                hud.clear();
            }
        } else {
            if (shouldBeVisible.getAsBoolean()) {
                switchContentTo(activeElements);
            }
        }

    }

    @Override
    public void clickEvent(int x, int y) {
        for (HudElement element : activeElements){
            if (element instanceof MenuClickable) {
                MenuClickable button = (MenuClickable) element;
                if (button.contains(x, y))
                button.onClick(x, y);
            }
        }
    }

    public MenuElement[] getActiveElements() {
        return activeElements;
    }
}
