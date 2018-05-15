package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.userinterface.*;
import nl.NG.Jetfightergame.Settings.ClientSettings;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen, Jorren Hendriks
 */
public class JetFighterMenu extends HudMenu { // TODO generalize the return button

    private final static String[] creditTextfield =
            ("Main producer:\n" +
                    "Geert van Ieperen\n" +
                    "\n" +
                    "With thanks to:\n" +
                    "Tim Beurskens\n" +
                    "Daan Drijver\n" +
                    "Daan de Graaf\n" +
                    "Jorren Hendriks\n" +
                    "Tom Peters\n" +
                    "Yoeri Poels\n" +
                    "\n" +
                    "A production of TU/entertainment"
            ).split("\n");

    private static final int TEXTFIELD_WIDTH = 750;

    private MenuClickable[] mainMenu;
    private MenuClickable[] optionMenu;
    private MenuClickable[] graphicsMenu;
    private MenuClickable[] controlsMenu;
    private UIElement[] creditScreen;

    public JetFighterMenu(Supplier<Integer> widthSupplier, Supplier<Integer> heightSupplier,
                          Runnable startGame, Runnable exitGame, ControllerManager input,
                          ShaderManager shaderManager, ScreenOverlay hud
    ) {
        super(widthSupplier, heightSupplier, hud);

        MenuClickable startGameButton = new MenuButton("Start Game", startGame);
        MenuClickable options = new MenuButton("Options", () -> switchContentTo(optionMenu));
        {
            MenuClickable graphics = graphicsMenu(shaderManager);
            MenuClickable controls = controlMenu(input);
            MenuClickable backOptions = new MenuButton("Back", () -> switchContentTo(mainMenu));
            optionMenu = new MenuClickable[]{graphics, controls, backOptions};
        }
        MenuClickable credits = new MenuButton("Credits", () -> switchContentTo(creditScreen));
        {
            UIElement credit = new MenuTitledText("Credits", creditTextfield, TEXTFIELD_WIDTH);
            MenuButton creditBackButton = new MenuButton("Back", () -> switchContentTo(mainMenu));
            creditScreen = new UIElement[]{credit, creditBackButton};
        }
        MenuClickable exitGameButton = new MenuButton("Exit Game", exitGame);
        mainMenu = new MenuClickable[]{startGameButton, options, credits, exitGameButton};

        switchContentTo(mainMenu);
    }

    private MenuClickable controlMenu(ControllerManager control) {
        MenuClickable controls = new MenuButton("Controls", () -> switchContentTo(controlsMenu));
        {
            MenuClickable invertX = new MenuToggle("Invert camera-x", (b) ->
                    ClientSettings.INVERT_CAMERA_ROTATION = !ClientSettings.INVERT_CAMERA_ROTATION);
            MenuClickable controllerType = new MenuToggleMultiple("Controller", control.names(), control::switchTo);
            MenuClickable backControls = new MenuButton("Back", () -> switchContentTo(optionMenu));
            controlsMenu = new MenuClickable[]{invertX, controllerType, backControls};
        }
        return controls;
    }

    private MenuClickable graphicsMenu(ShaderManager shaders) {
        MenuClickable graphics = new MenuButton("Graphics", () -> switchContentTo(graphicsMenu));
        {
            MenuToggleMultiple shader = new MenuToggleMultiple("Shader", shaders.names(), shaders::switchTo);
            MenuClickable backGraphics = new MenuButton("Back", () -> switchContentTo(optionMenu));
            graphicsMenu = new MenuClickable[]{shader, backGraphics};
        }
        return graphics;
    }

}
