package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.Tools.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.NG.Jetfightergame.Engine.GLFWGameEngine.GameMode.MENU_MODE;
import static nl.NG.Jetfightergame.Settings.KeyBindings.EXIT_GAME;
import static nl.NG.Jetfightergame.Settings.KeyBindings.TOGGLE_FULLSCREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PRINT_SCREEN;

/**
 * @author Geert van Ieperen. Created on 7-7-2018.
 */
public class ActionButtonHandler implements TrackerKeyListener {
    public static final int PRINT_SCREEN = GLFW_KEY_PRINT_SCREEN;
    public static final int START_GAME = GLFW_KEY_ENTER;

    private final JetFighterGame client;
    private final ClientConnection connection;

    public ActionButtonHandler(JetFighterGame game, ClientConnection connection) {
        this.client = game;
        this.connection = connection;
        KeyTracker.getInstance().addKeyListener(this);
    }

    @Override
    public void keyPressed(int key) {
        switch (key) {
            case START_GAME:
                connection.sendCommand(MessageType.START_GAME);
                break;

            case EXIT_GAME:
                if (client.getCurrentGameMode() == MENU_MODE) client.exitGame();
                else client.setMenuMode();
                break;

            case TOGGLE_FULLSCREEN:
                Logger.print("Switching fullscreen");
                client.getWindow().toggleFullScreen();
                break;

            case PRINT_SCREEN:
                SimpleDateFormat ft = new SimpleDateFormat("yy-mm-dd_hh_mm_ss");
                final String name = "Screenshot_" + ft.format(new Date());

                boolean success = client.getWindow().printScreen(name);
                if (success) {
                    Logger.print("Saved screenshot as \"" + name + "\"");
                }
                break;
        }
    }

    @Override
    public void cleanUp() {
        KeyTracker.getInstance().removeKeyListener(this);
    }
}
