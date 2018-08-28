package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.Userinterface.*;
import nl.NG.Jetfightergame.Settings.ClientSettings;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen, Jorren Hendriks
 */
public class JetFighterMenu extends HudMenu {

    private final static String[] creditTextfield =
            ("Main producer:\n" +
                    "Geert van Ieperen\n" +
                    "\n" +
                    "A production of TU/entertainment"
            ).split("\n");

    private static final int TEXTFIELD_WIDTH = 750;

    private MenuClickable[] mainMenu;
    private MenuClickable[] optionMenu;
    private UIElement[] creditScreen;

    public JetFighterMenu(
            Supplier<Integer> widthSupplier, Supplier<Integer> heightSupplier,
            Runnable startGame, Runnable exitGame, ControllerManager input,
            ShaderManager shaderManager, BooleanSupplier isMenuMode
    ) {
        super(widthSupplier, heightSupplier, isMenuMode);

        UIElement credit = new MenuTitledText("Credits", creditTextfield, TEXTFIELD_WIDTH);
        MenuButton creditBackButton = new MenuButton("Back", () -> switchContentTo(mainMenu));
        creditScreen = new UIElement[]{credit, creditBackButton};

        optionMenu = getOptions(shaderManager, input);
        mainMenu = getMainMenu(startGame, exitGame);

        switchContentTo(mainMenu);
    }

    private MenuClickable[] getMainMenu(Runnable startGame, Runnable exitGame) {
        MenuClickable startGameButton = new MenuButton("Start Game", startGame);
        MenuClickable options = new MenuButton("Options", () -> switchContentTo(optionMenu));
        MenuClickable credits = new MenuButton("Credits", () -> switchContentTo(creditScreen));
        MenuClickable exitGameButton = new MenuButton("Exit Game", exitGame);
        return new MenuClickable[]{startGameButton, options, credits, exitGameButton};
    }

    private MenuClickable[] getOptions(ShaderManager shaderManager, ControllerManager input) {
        MenuToggleMultiple shader = new MenuToggleMultiple("Shader", shaderManager.names(), shaderManager::switchTo);
        MenuClickable invertX = new MenuToggle("Invert camera-x", (b) ->
                ClientSettings.INVERT_CAMERA_ROTATION = b
        );
        MenuClickable controllerType = new MenuToggleMultiple("Controller", input.names(), input::switchTo);
        MenuClickable backOptions = new MenuButton("Back", () -> switchContentTo(mainMenu));
        return new MenuClickable[]{shader, invertX, controllerType, backOptions};
    }

    public void appendToMain(MenuClickable component) {
        int lastInd = mainMenu.length;
        mainMenu = Arrays.copyOf(mainMenu, lastInd + 1);
        mainMenu[lastInd] = mainMenu[lastInd - 1]; // exit game button
        mainMenu[lastInd - 1] = component;
        switchContentTo(mainMenu);
    }
}
