package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

import static nl.NG.Jetfightergame.Settings.ClientSettings.JET_COLOR;

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
            Assert.fail();
        }
    }

    private Exception tryConnect(Socket client) {
        try {
            client.connect(new InetSocketAddress(ServerSettings.SERVER_PORT));
            ClientConnection cc = new ClientConnection("TheLegend27",
                    client.getOutputStream(), client.getInputStream(), EntityClass.JET_SPITZ, JET_COLOR
            );
            Logger.DEBUG.print("received a " + cc);
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