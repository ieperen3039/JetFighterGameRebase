package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Scenarios.CollisionLaboratory;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

/**
 * @author Geert van Ieperen created on 15-5-2018.
 */
public class JetFighterServerTest {

    @Test(timeout = 5000)
    public void main() {
        Socket client = new Socket();

        new Thread(() -> Objects.requireNonNull(tryMain()).printStackTrace()).start();
        Exception ex2 = tryConnect(client);

        if (ex2 != null){
            ex2.printStackTrace();
        }
    }

    private Exception tryConnect(Socket client) {
        try {
            client.connect(new InetSocketAddress(ServerSettings.SERVER_PORT));
            ClientConnection cc = new ClientConnection(Controller.EMPTY, client, new CollisionLaboratory(new GameTimer()));
            Logger.print("received a " + cc.getPlayer());
        } catch (IOException e) {
            return e;
        }

        return null;
    }

    private Exception tryMain() {
        try {
            JetFighterServer.main(new String[0]);
        } catch (IOException e) {
            return e;
        }

        return null;
    }
}