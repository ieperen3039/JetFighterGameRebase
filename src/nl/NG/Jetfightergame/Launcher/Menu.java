package nl.NG.Jetfightergame.Launcher;

import nl.NG.Jetfightergame.ScreenOverlay.HudMenu;
import nl.NG.Jetfightergame.ScreenOverlay.Userinterface.MenuButton;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 21-5-2018.
 */
public class Menu extends HudMenu {

    public Menu(Runnable startGame, Supplier<Integer> getWidth, Supplier<Integer> getHeight) {
        super(getWidth, getHeight, () -> true);

        MenuButton startOffline = new MenuButton("Start Offline Server", startGame);

        switchContentTo(new MenuButton[]{startOffline});
    }

}
