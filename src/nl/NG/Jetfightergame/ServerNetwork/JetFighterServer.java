package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromFile;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

import static nl.NG.Jetfightergame.ServerNetwork.JetFighterServer.Phase.*;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public class JetFighterServer {

    private final int portNumber;
    private ServerSocket socket;
    private Socket terminalSocket;

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
     * @param environment the world to simulate in. Do not use this object afterward
     * @param loadModels whether the server should trigger a load of the models.
     *                   set this to true iff the server is not running as offline server (i.e. sharing objects)
     * @throws IOException if a serversocket could not be created
     */
    private JetFighterServer(Environment environment, boolean loadModels) throws IOException {
        currentPhase = BOOTING;

        this.socket = new ServerSocket(ServerSettings.SERVER_PORT);
        this.game = new ServerLoop(environment, e -> {});

        environment.buildScene(game, ServerSettings.COLLISION_DETECTION_LEVEL, true);
        portNumber = socket.getLocalPort();

        if (loadModels) {
            ShapeFromFile.init(false);
            GeneralShapes.init(false);
        }

        currentPhase = WAITING_FOR_HOST;
    }

    /**
     * creates a thread on the local machine, connecting the given socket as host.
     * @param worldCreator a constructor for the world on which is played
     * @param client the connection between front-end and back-end
     * @throws IOException if the initialisation runs into problems
     */
    public static AbstractGameLoop createOfflineServer(Function<GameTimer, Environment> worldCreator, Socket client) throws IOException {
        Environment world = worldCreator.apply(new GameTimer());

        JetFighterServer server = new JetFighterServer(world, false);
        // internal connect
        new Thread(server::listenForHost).start();
        client.connect(new InetSocketAddress(server.portNumber));

        server.currentPhase = STARTING;
        return server.getGameLoop();
    }

    /**
     * blocks until one connection is made.
     * The host of this server is set to the next connection.
     * If {@link #terminateListen()} is called, it will break, reverting to the state as before calling the method
     */
    public void listenForHost() {
        if ((currentPhase != WAITING_FOR_HOST) && (currentPhase != PREPARATION)) {
            throw new IllegalStateException("listenForHost() was called in phase " + currentPhase + " phase");
        }

        try {
            Toolbox.print("Waiting for host on port " + portNumber + " on address " + socket.getInetAddress());
            acceptConnection(true);
            currentPhase = PREPARATION;

        } catch (IOException ex){
            Toolbox.printError(ex);
        }
    }

    /**
     * listens to the predefined port, adding all incoming requests to clients.
     * blocks while listening, and only stops when {@link #terminateListen()} is called
     * @throws IOException if an I/O error occurs when waiting for a connection
     */
    public void listen() throws IOException {
        if (currentPhase != PREPARATION) throw new IllegalStateException("listen() was called in " + currentPhase + " phase");
        Toolbox.print("Listening to port " + portNumber + " on address " + socket.getInetAddress());

        while ((currentPhase == PREPARATION) && acceptConnection(false));

        Toolbox.print("Stopped listening for new players");
        currentPhase = STARTING;
    }

    public boolean acceptConnection(boolean asAdmin) throws IOException {
        Socket client = socket.accept();
        if (client == terminalSocket) return false;

        Toolbox.print("Connection made with " + client + (asAdmin ? " with admin privileges": ""));
        game.connectToPlayer(client, asAdmin);
        return true;
    }

    /**
     * initialises the physics engine and returns it
     * @return the gameloop, paused and not started
     */
    public AbstractGameLoop getGameLoop() {
        if (currentPhase != STARTING) throw new IllegalStateException("getGameLoop() was called in " + currentPhase + " phase");
        currentPhase = RUNNING;
        return game;
    }

    /**
     * sends a terminal message to the current port,
     * terminating any thread waiting in {@link #listen()} or {@link #listenForHost()}
     */
    private void terminateListen() {
        try {
            terminalSocket = new Socket((String) null, portNumber);
            terminalSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** @return what the server is doing ATM */
    public Phase getPhase() {
        return currentPhase;
    }

    /** starts a test-server */
    public static void main(String[] args) throws IOException {
        GameTimer time = new GameTimer();
        Environment world = new PlayerJetLaboratory(time);
        JetFighterServer server = new JetFighterServer(world, true);
        server.listenForHost();

//        server.listen();
        server.currentPhase = STARTING;

        AbstractGameLoop game = server.getGameLoop();
        game.run();
    }
}