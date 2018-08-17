package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Tools.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static nl.NG.Jetfightergame.Settings.ServerSettings.SERVER_PORT;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public class JetFighterServer implements BlockingListener {
    private ServerSocket socket;
    private ServerLoop game;

    public AbstractGameLoop getRunnable() {
        return game;
    }

    /**
     * starts a single environment to run exactly once.
     * @param world the world to simulate in.
     * @throws IOException if a serversocket could not be created
     */
    public JetFighterServer(EnvironmentClass world) throws IOException {
        this.socket = new ServerSocket(SERVER_PORT);
        this.game = new ServerLoop(EnvironmentClass.LOBBY, world);
    }

    /**
     * connects a new player on the given input and output streams
     * @param asHost if true, player is considered to host the server
     */
    public void shortConnect(InputStream receive, OutputStream send, boolean asHost) {
        try {
            Logger.DEBUG.print("Creating internal connection" + (asHost ? " with host privileges" : ""));
            game.connectToPlayer(receive, send, asHost);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * blocks until one connection is made. The host of this server is set to the next connection.
     */
    public void listenForHost() {
        try {
            Logger.DEBUG.print("Waiting for host on port " + socket.getLocalPort() + " on address " + socket.getInetAddress());
            acceptConnection(true);

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    /**
     * listens to the predefined port, adding all incoming requests to clients. blocks while listening.
     */
    public boolean handleMessage() throws IOException {
        acceptConnection(false);
        return true;
    }

    public void acceptConnection(boolean asAdmin) throws IOException {
        Socket client = socket.accept();

        Logger.DEBUG.print("Connection made with " + client + (asAdmin ? " with host privileges" : ""));
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        game.connectToPlayer(in, out, asAdmin);
    }

    /** starts a server */
    public static void main(String[] args) throws IOException {
        GeneralShapes.init(false);
        CustomJetShapes.init(false);
        JetFighterServer server = new JetFighterServer(EnvironmentClass.ISLAND_MAP);
        server.listenForHost();

        server.listenInThread(true);
        Logger.DEBUG.print("Listening to port " + server.socket.getLocalPort() + " on address " + server.socket.getInetAddress());

        server.game.run();
    }
}
