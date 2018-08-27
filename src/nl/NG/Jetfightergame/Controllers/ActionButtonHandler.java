package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.NG.Jetfightergame.Engine.JetFighterGame.GameMode.MENU_MODE;
import static nl.NG.Jetfightergame.Settings.KeyBinding.*;

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
        if (START_GAME.is(key)) {
            connection.sendCommand(MessageType.START_GAME);

        } else if (EXIT_GAME.is(key)) {
            if (client.getCurrentGameMode() == MENU_MODE) client.exitGame();
            else client.setMenuMode();

        } else if (TOGGLE_FULLSCREEN.is(key)) {
            Logger.DEBUG.print("Switching fullscreen");
            client.getWindow().toggleFullScreen();

        } else if (PRINT_SCREEN.is(key)) {
            SimpleDateFormat ft = new SimpleDateFormat("mm_dd-hh_mm_ss");
            final String name = "Screenshot_" + ft.format(new Date());

            client.getWindow().printScreen(Directory.screenShots, true, name);
            Logger.DEBUG.print("Saved screenshot as \"" + name + "\"");

        } else if (TOGGLE_DEBUG_SCREEN.is(key)) {
            ClientSettings.DEBUG_SCREEN = !ClientSettings.DEBUG_SCREEN;

        } else if (DISABLE_HUD.is(key)) {
            client.toggleHud();
        }
    }

    @Override
    public void cleanUp() {
        KeyTracker.getInstance().removeKeyListener(this);
    }
}
