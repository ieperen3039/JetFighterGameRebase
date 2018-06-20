package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.*;
import nl.NG.Jetfightergame.Settings.ClientSettings;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen, Jorren Hendriks
 */
public class JetFighterMenu extends HudMenu { // TODO generalize the return button

    private final static String[] creditTextfield =
            ("Main producer:\n" +
                    "Geert van Ieperen\n" +
                    "\n" +
                    "A production of TU/entertainment"
            ).split("\n");

    private static final int TEXTFIELD_WIDTH = 750;

    private MenuClickable[] mainMenu;
    private MenuClickable[] optionMenu;
    private MenuClickable[] graphicsMenu;
    private MenuClickable[] controlsMenu;
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

        graphicsMenu = getGraphics(shaderManager);
        optionMenu = getOptions();
        controlsMenu = getControlMenu(input);
        mainMenu = getMainMenu(startGame, exitGame);

        switchContentTo(mainMenu);
    }

    private MenuClickable[] getGraphics(ShaderManager shaderManager) {
        MenuToggleMultiple shader = new MenuToggleMultiple("Shader", shaderManager.names(), shaderManager::switchTo);
        MenuClickable backGraphics = new MenuButton("Back", () -> switchContentTo(optionMenu));
        return new MenuClickable[]{shader, backGraphics};
    }

    private MenuClickable[] getMainMenu(Runnable startGame, Runnable exitGame) {
        MenuClickable startGameButton = new MenuButton("Start Game", startGame);
        MenuClickable options = new MenuButton("Options", () -> switchContentTo(optionMenu));
        MenuClickable credits = new MenuButton("Credits", () -> switchContentTo(creditScreen));
        MenuClickable exitGameButton = new MenuButton("Exit Game", exitGame);
        return new MenuClickable[]{startGameButton, options, credits, exitGameButton};
    }

    private MenuClickable[] getOptions() {
        MenuClickable graphics1 = new MenuButton("Graphics", () -> switchContentTo(graphicsMenu));
        MenuClickable controls1 = new MenuButton("Controls", () -> switchContentTo(controlsMenu));
        MenuClickable backOptions = new MenuButton("Back", () -> switchContentTo(mainMenu));
        return new MenuClickable[]{graphics1, controls1, backOptions};
    }

    private MenuClickable[] getControlMenu(ControllerManager input) {
        MenuClickable invertX = new MenuToggle("Invert camera-x", (b) ->
                ClientSettings.INVERT_CAMERA_ROTATION = !ClientSettings.INVERT_CAMERA_ROTATION
        );
        MenuClickable controllerType = new MenuToggleMultiple("Controller", input.names(), input::switchTo);
        MenuClickable backControls = new MenuButton("Back", () -> switchContentTo(optionMenu));
        return new MenuClickable[]{invertX, controllerType, backControls};
    }
}
