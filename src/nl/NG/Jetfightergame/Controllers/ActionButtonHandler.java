package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.NG.Jetfightergame.Engine.JetFighterGame.GameMode.MENU_MODE;
import static nl.NG.Jetfightergame.Settings.KeyBindings.*;

/**
 * @author Geert van Ieperen. Created on 7-7-2018.
 */
public class ActionButtonHandler implements TrackerKeyListener {
    private final JetFighterGame client;
    private final ClientConnection connection;

    public ActionButtonHandler(JetFighterGame game, ClientConnection connection) {
        this.client = game;
        this.connection = connection;
        KeyTracker.getInstance().addKeyListener(this);
    }

    @Override
    public void keyPressed(int key) {
        if (key == START_GAME) {
            connection.sendCommand(MessageType.START_GAME);

        } else if (key == EXIT_GAME) {
            if (client.getCurrentGameMode() == MENU_MODE) client.exitGame();
            else client.setMenuMode();

        } else if (key == TOGGLE_FULLSCREEN) {
            Logger.DEBUG.print("Switching fullscreen");
            client.getWindow().toggleFullScreen();

        } else if (key == PRINT_SCREEN) {
            SimpleDateFormat ft = new SimpleDateFormat("yy-mm-dd_hh_mm_ss");
            final String name = "Screenshot_" + ft.format(new Date());

            boolean success = client.getWindow().printScreen(name);
            if (success) {
                Logger.DEBUG.print("Saved screenshot as \"" + name + "\"");
            }
        } else if (key == TOGGLE_DEBUG_SCREEN) {
            ClientSettings.DEBUG_SCREEN = !ClientSettings.DEBUG_SCREEN;
        }
    }

    @Override
    public void cleanUp() {
        KeyTracker.getInstance().removeKeyListener(this);
    }
}
