package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Tools.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static nl.NG.Jetfightergame.ServerNetwork.JetFighterServer.Phase.*;
import static nl.NG.Jetfightergame.Settings.ServerSettings.SERVER_PORT;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public class JetFighterServer {

    private final int portNumber;
    private ServerSocket socket;

    private Phase currentPhase;
    private ServerLoop game;

    public AbstractGameLoop getRunnable() {
        return game;
    }

    public enum Phase {
        BOOTING, // server is creating a map
        WAITING_FOR_HOST, // before the host has connected
        PREPARATION, // players can join
        STARTING, // world is being set up
        RUNNING, // game is running
        FINISHED // game is finished, server is done
    }

    /**
     * starts a single environment to run exactly once.
     * @param world the world to simulate in.
     * @throws IOException if a serversocket could not be created
     */
    public JetFighterServer(EnvironmentClass world) throws IOException {
        currentPhase = BOOTING;

        this.socket = new ServerSocket(SERVER_PORT);
        this.game = new ServerLoop(EnvironmentClass.LOBBY, world);
        portNumber = socket.getLocalPort();

        currentPhase = WAITING_FOR_HOST;
    }

    /**
     * creates a thread on the local machine, connecting the given socket as host.
     * @param type the type of world to start this server on
     * @param client       the connection between front-end and back-end
     * @throws IOException if the initialisation runs into problems
     */
    public static AbstractGameLoop createOfflineServer(EnvironmentClass type, Socket client) throws IOException {

        JetFighterServer server = new JetFighterServer(type);
        new Thread(server::listenForHost).start();
        client.connect(new InetSocketAddress(server.portNumber), 1000);

        return server.game;
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
        if ((currentPhase != WAITING_FOR_HOST) && (currentPhase != PREPARATION)) {
            throw new IllegalStateException("listenForHost() was called in phase " + currentPhase + " phase");
        }

        try {
            Logger.DEBUG.print("Waiting for host on port " + portNumber + " on address " + socket.getInetAddress());
            acceptConnection(true);
            currentPhase = PREPARATION;

        } catch (IOException ex) {
            Logger.ERROR.print(ex);
        }
    }

    /**
     * listens to the predefined port, adding all incoming requests to clients. blocks while listening.
     * @throws IOException if an I/O error occurs when waiting for a connection
     */
    public void listen() throws IOException {
        if (currentPhase != PREPARATION)
            throw new IllegalStateException("listen() was called in " + currentPhase + " phase");
        Logger.DEBUG.print("Listening to port " + portNumber + " on address " + socket.getInetAddress());

        while (currentPhase == PREPARATION) {
            acceptConnection(false);
        }

        Logger.DEBUG.print("Stopped listening for new players");
        currentPhase = STARTING;
    }

    public void acceptConnection(boolean asAdmin) throws IOException {
        Socket client = socket.accept();

        Logger.DEBUG.print("Connection made with " + client + (asAdmin ? " with host privileges" : ""));
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        game.connectToPlayer(in, out, asAdmin);
    }

    /** @return what the server is doing ATM */
    public Phase getPhase() {
        return currentPhase;
    }

    /** starts a test-server */
    public static void main(String[] args) throws IOException {
        GeneralShapes.init(false);
        CustomJetShapes.init(false);
        JetFighterServer server = new JetFighterServer(EnvironmentClass.ISLAND_MAP);
        server.listenForHost();

//        server.listen();
        server.currentPhase = STARTING;
        server.game.run();
    }
}
