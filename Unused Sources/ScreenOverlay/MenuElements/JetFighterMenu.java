package nl.NG.Jetfightergame.ScreenOverlay.MenuElements;

import nl.NG.Jetfightergame.Engine.MusicProvider;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;

import java.util.function.BooleanSupplier;

/**
 * @author Geert van Ieperen, Jorren Hendriks
 */
public class JetFighterMenu extends HudMenu {

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
    private static final int TEXTFIELD_HEIGHT = 450;

    private MenuClickable[] mainMenu;
    private MenuClickable[] optionMenu;
    private MenuClickable[] graphicsMenu;
    private MenuClickable[] audioMenu;
    private MenuClickable[] controlsMenu;
    private MenuElement[] creditScreen;

    public JetFighterMenu(MusicProvider musicProvider, Hud hud, BooleanSupplier shouldBeVisible, Runnable startGame, Runnable exitGame) {
        super(hud, shouldBeVisible);

        MenuClickable startGameButton = new MenuButton("Start Game", startGame);
        MenuClickable options = new MenuButton("Options", () -> switchContentTo(optionMenu));
        {
            MenuClickable graphics = graphicsMenu();
            MenuClickable audio = audioMenu(musicProvider);
            MenuClickable controls = controlMenu();
            MenuClickable backOptions = new MenuButton("Back", () -> switchContentTo(mainMenu));
            optionMenu = new MenuClickable[]{graphics, audio, controls, backOptions};
        }
        MenuClickable credits = new MenuButton("Credits", () -> switchContentTo(creditScreen));
        {
            MenuElement credit = new MenuTextField("Credits", creditTextfield, TEXTFIELD_WIDTH, TEXTFIELD_HEIGHT);
            MenuButton creditBackButton = new MenuButton("Back", () -> switchContentTo(mainMenu));
            creditScreen = new MenuElement[]{credit, creditBackButton};
        }
        MenuClickable exitGameButton = new MenuButton("Exit Game", exitGame);
        mainMenu = new MenuClickable[]{startGameButton, options, credits, exitGameButton};

        switchContentTo(mainMenu);
    }

    private MenuClickable controlMenu() {
        MenuClickable controls = new MenuButton("Controls", () -> switchContentTo(controlsMenu));
        {
            MenuClickable InvertedX = new MenuToggle("Invert x-axis", (b) ->
                    Settings.INVERT_CAMERA_ROTATION = !Settings.INVERT_CAMERA_ROTATION);
            MenuClickable backControls = new MenuButton("Back", () -> switchContentTo(optionMenu));
            controlsMenu = new MenuClickable[]{InvertedX, backControls};
        }
        return controls;
    }

    private MenuClickable audioMenu(MusicProvider musicProvider) {
        MenuClickable audio = new MenuButton("Audio", () -> switchContentTo(audioMenu));
        {
            MenuClickable master = new MenuSlider("Volume", (i) -> musicProvider.setBaseVolume((i < 0.05f ? 0.05f : i)));
            MenuToggle toggleAudio = new MenuToggle("Music", (i) -> musicProvider.toggle());
            toggleAudio.setValue(musicProvider.isOn());
            MenuClickable backAudio = new MenuButton("Back", () -> switchContentTo(optionMenu));
            audioMenu = new MenuClickable[]{master, toggleAudio, backAudio};
        }
        return audio;
    }

    private MenuClickable graphicsMenu() {
        MenuClickable graphics = new MenuButton("Graphics", () -> switchContentTo(graphicsMenu));
        {
            MenuClickable backGraphics = new MenuButton("Back", () -> switchContentTo(optionMenu));
            graphicsMenu = new MenuClickable[]{backGraphics};
        }
        return graphics;
    }

}
