package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Tools.Logger;

import java.io.IOException;
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
     * @param loadModels  whether the server should trigger a load of the models. set this to true iff the server is not
     *                    running as offline server (i.e. sharing objects)
     * @throws IOException if a serversocket could not be created
     */
    private JetFighterServer(EnvironmentClass world, boolean loadModels) throws IOException {
        currentPhase = BOOTING;

        if (loadModels) {
            GeneralShapes.init(false);
        }

        this.socket = new ServerSocket(SERVER_PORT);
        this.game = new ServerLoop(world);
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

        JetFighterServer server = new JetFighterServer(type, false);
        // internal connect, this thread will soon terminate
        new Thread(server::listenForHost).start();
        client.connect(new InetSocketAddress(server.portNumber), 1000);

        return server.game;
    }

    /**
     * blocks until one connection is made. The host of this server is set to the next connection.
     */
    public void listenForHost() {
        if ((currentPhase != WAITING_FOR_HOST) && (currentPhase != PREPARATION)) {
            throw new IllegalStateException("listenForHost() was called in phase " + currentPhase + " phase");
        }

        try {
            Logger.print("Waiting for host on port " + portNumber + " on address " + socket.getInetAddress());
            acceptConnection(true);
            currentPhase = PREPARATION;

        } catch (IOException ex) {
            Logger.printError(ex);
        }
    }

    /**
     * listens to the predefined port, adding all incoming requests to clients. blocks while listening.
     * @throws IOException if an I/O error occurs when waiting for a connection
     */
    public void listen() throws IOException {
        if (currentPhase != PREPARATION)
            throw new IllegalStateException("listen() was called in " + currentPhase + " phase");
        Logger.print("Listening to port " + portNumber + " on address " + socket.getInetAddress());

        while ((currentPhase == PREPARATION) && acceptConnection(false)) ;

        Logger.print("Stopped listening for new players");
        currentPhase = STARTING;
    }

    public boolean acceptConnection(boolean asAdmin) throws IOException {
        Socket client = socket.accept();

        Logger.print("Connection made with " + client + (asAdmin ? " with admin privileges" : ""));
        game.connectToPlayer(client, asAdmin);
        return true;
    }

    /**
     * Stop accepting connections, and open a portal to the new world. Possible delay between these two actions should
     * be handled in the lobby world.
     * @param world the world to start
     */
    public void upgrade(EnvironmentClass world) {
        currentPhase = STARTING;
        // TODO stop listen() calls connections
        game.startMap(world);
    }

    /** @return what the server is doing ATM */
    public Phase getPhase() {
        return currentPhase;
    }

    /** starts a test-server */
    public static void main(String[] args) throws IOException {
        JetFighterServer server = new JetFighterServer(EnvironmentClass.PLAYERJET_LABORATORY, true);
        server.listenForHost();

//        server.listen();
        server.currentPhase = STARTING;
        server.game.run();
    }
}
