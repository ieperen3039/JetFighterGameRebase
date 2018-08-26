package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Camera.CameraFocusMovable;
import nl.NG.Jetfightergame.Camera.CameraManager;
import nl.NG.Jetfightergame.Controllers.ActionButtonHandler;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.PowerupDisplay;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.RaceProgressDisplay;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ScreenOverlay.Userinterface.MenuToggleMultiple;
import nl.NG.Jetfightergame.ServerNetwork.*;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.StreamPipe;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.FollowingCamera;
import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.SpectatorFollowing;
import static nl.NG.Jetfightergame.Rendering.JetFighterRenderer.Mode.*;

/**
 * @author Geert van Ieperen created on 29-10-2017.
 */
public class JetFighterGame {

    private final ActionButtonHandler actionHandler;
    private GLFWWindow window;
    private GameMode currentGameMode;
    private JetFighterRenderer renderLoop;
    private Collection<AbstractGameLoop> otherLoops = new HashSet<>();

    private ClientConnection connection;
    private final CameraManager camera;
    private final List<Runnable> closeOperations = new ArrayList<>();

    /**
     * Shows a splash screen, and creates a window in which the game runs
     * @param doShow if false, the game will run headless (for recording)
     * @param doStore if true, a video of the replay file will be recorded
     * @param replayFile a replay file to play, or null if no file should be played
     * @param map the map for racing
     * @param hostAddress address of server to find. When null, a new local server will be created
     */
    public JetFighterGame(boolean doShow, boolean doStore, File replayFile, EnvironmentClass map, InetAddress hostAddress) throws Exception {
        Logger.INFO.print("Starting the game...");
        Logger.DEBUG.print("General debug information: " +
                "\n\tSystem OS:          " + System.getProperty("os.name") +
                "\n\tJava VM:            " + System.getProperty("java.runtime.version") +
                "\n\tWorking directory:  " + Directory.currentDirectory() +
                "\n\tProtocol version:   " + JetFighterProtocol.versionNumber +
                (hostAddress == null ? "\n\tLocal server enabled" : "") +
                (replayFile == null ? "" : "\n\tReplay file:        " + replayFile.getName()) +
                (replayFile != null && doStore ? "\n\tCreating video from replay" : "") +
                (replayFile == null && doStore ? "\n\tStoring game state enabled" : "") +
                (doShow ? "" : "\n\tHeadless mode enabled")
        );


        if (!doShow && replayFile == null) throw new IllegalArgumentException("No show without replay file");

        Splash splash = new Splash();
        splash.run();

        try {
            this.window = new GLFWWindow(ServerSettings.GAME_NAME, 1600, 900, true);
            closeOperations.add(window::cleanup);

            GeneralShapes.init(true);

            MouseTracker.getInstance().setGameModeDecision(() -> currentGameMode != GameMode.MENU_MODE);
            MouseTracker.getInstance().listenTo(window);
            KeyTracker.getInstance().listenTo(window);

            //        new SoundEngine();
            //        Sounds.initAll(); // TODO also enable checkALError() in exitGame()


            if (replayFile == null) {
                String playerName = "TheLegend" + Toolbox.random.nextInt(1000);

                OutputStream sendChannel;
                InputStream receiveChannel;

                if (hostAddress == null) {
                    Logger.INFO.print("Creating new local server");

                    JetFighterServer server = new JetFighterServer(map, doStore);

                    if (ClientSettings.USE_SOCKET_FOR_OFFLINE) {
                        new Thread(server::listenForHost).start();

                        Socket client = new Socket(InetAddress.getLocalHost(), ServerSettings.SERVER_PORT);
                        sendChannel = client.getOutputStream();
                        receiveChannel = client.getInputStream();

                    } else {
                        StreamPipe serverToClient = new StreamPipe(1024);
                        StreamPipe clientToServer = new StreamPipe(256);

                        InputStream serverReceive = serverToClient.getInputStream();
                        OutputStream serverSend = clientToServer.getOutputStream();
                        new Thread(() -> server.shortConnect(serverReceive, serverSend, true)).start();

                        sendChannel = serverToClient.getOutputStream();
                        receiveChannel = clientToServer.getInputStream();
                    }

                    server.listenInThread(true);
                    AbstractGameLoop serverLoop = server.getRunnable();
                    serverLoop.setDaemon(true);
                    serverLoop.start();

                    closeOperations.add(server::close);

                } else {
                    Logger.INFO.print("Searching local server");
                    Socket socket;

                    try {
                        socket = new Socket(hostAddress, ServerSettings.SERVER_PORT);
                    } catch (ConnectException ex) {
                        Logger.WARN.print("Could not find local server");
                        throw ex;
                    }

                    sendChannel = socket.getOutputStream();
                    receiveChannel = socket.getInputStream();
                }

                connection = new ClientConnection(playerName, sendChannel, receiveChannel, ClientSettings.JET_TYPE);
                otherLoops.add(connection);
                Logger.printOnline(() -> connection.getTimer().toString());

                ClientControl player = connection;
                Environment gameState = connection.getWorld();
                AbstractJet playerJet = player.jet();
                Logger.DEBUG.print("Received " + playerJet + " from the server");

                camera = new CameraManager();
                camera.switchTo(FollowingCamera, new PosVector(-5, 4, 2), player, gameState, DirVector.zVector());
                Consumer<ScreenOverlay.Painter> hud = new GravityHud(playerJet, camera)
                        .andThen(new PowerupDisplay(player))
                        .andThen(connection.countDownGui())
                        .andThen(new RaceProgressDisplay(connection));

                renderLoop = new JetFighterRenderer(
                        this, gameState, window, camera, player.getInputControl(), hud, SHOW
                );

            } else { // file != null, start a replay file
                Logger.INFO.print("Starting replay of " + replayFile.getPath());

                camera = new CameraManager();
                EntityFactory focus = new CameraFocusMovable.Factory(new PosVector(0, 0, 0), new Quaternionf(), false);

                StateReader reader = new StateReader(replayFile, !doStore, focus, camera, this::exitGame);
                connection = reader;
                otherLoops.add(connection);
                Environment gameState = connection.getWorld();
                ControllerManager controls = connection.getInputControl();

                camera.switchTo(SpectatorFollowing, new PosVector(0, 0, 500), connection, gameState, DirVector.zVector());

                RaceProgressDisplay raceHud = new RaceProgressDisplay(connection);
                JetFighterRenderer.Mode renderMode = doStore ? (doShow ? RECORD_AND_SHOW : RECORD) : SHOW;
                JetFighterRenderer renderer = new JetFighterRenderer(this, gameState, window, camera, controls, raceHud, renderMode);
                this.renderLoop = renderer;

                StateReader.SpectatorModus[] values = StateReader.SpectatorModus.values();
                renderer.getMainMenu().appendToMain(new MenuToggleMultiple("Camera Modus",
                        Toolbox.toStringArray(values), (i) -> reader.setModus(values[i]))
                );
            }

            actionHandler = new ActionButtonHandler(this, connection);
            closeOperations.add(actionHandler::cleanUp);

            // set currentGameMode and engine.isPaused
            if (doShow) setMenuMode();
            else setPlayMode();
            connection.listenInThread(true);

        } catch (Exception anyException) {
            cleanup();
            throw anyException;

        } finally {
            // remove splash frame
            splash.dispose();
        }

        // reclaim all space used for initialisation
        System.gc();
        Logger.INFO.print("Initialisation complete\n");
        if (doShow) window.open();
    }

    public void setMenuMode() {
        currentGameMode = GameMode.MENU_MODE;
        window.freePointer();
        otherLoops.forEach(AbstractGameLoop::pause);
    }

    public void setPlayMode() {
        currentGameMode = GameMode.PLAY_MODE;
        window.capturePointer();
        otherLoops.forEach(AbstractGameLoop::unPause);
    }

    public GameTimer getTimer() {
        return connection.getTimer();
    }

    /**
     * Starts all threads and blocks until rendering has terminated again.
     * must be called in the current GL context
     */
    public void root() {
        Logger.DEBUG.print("Starting " + otherLoops);
        otherLoops.forEach(Thread::start);

        try {
            renderLoop.unPause();
            renderLoop.run(); // blocks until the client has quit

            for (AbstractGameLoop gameLoop : otherLoops) {
                Logger.DEBUG.print("Waiting for " + gameLoop + " to stop");
                gameLoop.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
            renderLoop.interrupt();
            otherLoops.forEach(Thread::interrupt);

        } finally {
            cleanup();
        }

        Logger.INFO.print("Game has stopped! Bye ~");
        // Finish execution
    }

    private void cleanup() {
        if (ClientSettings.CLEAN_AFTER_GAME) {
            GeneralShapes.cleanAll();
            AudioFile.cleanAll();
            SoundEngine.closeDevices();
        }

        closeOperations.forEach(Runnable::run);

        Toolbox.checkGLError();
//            Toolbox.checkALError();
    }

    /** tells the gameloops to stop */
    public void exitGame() {
        // wait for possible printing
        Toolbox.waitFor(10);

        System.out.println();
        Logger.INFO.print("Stopping game...");

        renderLoop.stopLoop();
        otherLoops.forEach(AbstractGameLoop::stopLoop);
    }

    /**
     * Get the {@link GLFWWindow} of the currently running instance.
     * @return The currently active Window.
     */
    public GLFWWindow getWindow() {
        return window;
    }

    public boolean isStopping() {
        return window.shouldClose();
    }

    public GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public boolean isPaused() {
        return currentGameMode != GameMode.PLAY_MODE;
    }

    public void toggleHud() {
        renderLoop.toggleHud();
    }

    public enum GameMode {
        PLAY_MODE, MENU_MODE, SPECTATOR_MODE
    }

    /**
     * @param argArray The arguments of the program.
     * @throws Exception if anything goes terribly wrong
     */
    public static void main(String... argArray) throws Exception {
        List<String> args = Arrays.asList(argArray);
        System.out.println("args: " + args);

        boolean makeLocalServer = args.contains("-local");
        ServerSettings.DEBUG = args.contains("-debug");
        boolean playReplay = args.contains("-replay");
        boolean storeReplay = args.contains("-store");
        int mapNameArg = args.indexOf("-map") + 1;
        File file = null;
        EnvironmentClass serverMap = EnvironmentClass.ISLAND_MAP;

        if (mapNameArg > 0) serverMap = EnvironmentClass.valueOf(args.get(mapNameArg));

        if (playReplay || storeReplay) {
            for (String arg : args) {
                if (arg.endsWith(StateWriter.EXTENSION)) {
                    file = Directory.recordings.getFile(arg);
                    if (!file.exists()) file = null;
                }
            }
            if (file == null) {
                Frame frame = new Frame();
                try {
                    FileDialog fd = new FileDialog(frame, "Select a replay file");
                    fd.setVisible(true);

                    File[] f = fd.getFiles();
                    if (f.length == 0) {
                        throw new IllegalArgumentException("Replay cancelled by user");
                    }
                    file = f[0];

                } finally {
                    frame.dispose();
                }
            }
        }
        InetAddress localHost = makeLocalServer ? null : InetAddress.getLocalHost();

        Logger.setLoggingLevel(ServerSettings.DEBUG ? Logger.DEBUG : Logger.INFO);
        boolean doShow = (file == null) || playReplay;
        new JetFighterGame(doShow, storeReplay, file, serverMap, localHost).root();
    }

    /**
     * a splash image that can be shown and disposed.
     */
    private class Splash extends Frame implements Runnable {

        Splash() {
            setTitle("Loading " + ServerSettings.GAME_NAME);
            // TODO better splash image
            final String image = "SplashImage.png";

            try {
                final BufferedImage splashImage = ImageIO.read(Directory.pictures.getFile(image));
                setImage(this, splashImage);
            } catch (Exception e) {
                Logger.ERROR.print("Could not load splash image " + Directory.pictures.getFile(image));
                e.printStackTrace();
                setSize(new Dimension(500, 300));
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Point centerPoint = ge.getCenterPoint();

            int dx = centerPoint.x - (getWidth() / 2);
            int dy = centerPoint.y - (getHeight() / 2);

            setLocation(dx, dy);
            setUndecorated(true);
            setBackground(Color.WHITE);
        }

        /**
         * makes a frame identical to the image, also adapts size
         * @param target some frame
         * @param image  some image
         */
        private void setImage(Frame target, final BufferedImage image) {
            target.add(new Component() {
                @Override
                public void paint(Graphics g) {
                    g.drawImage(image, 0, 0, null);
                }
            });
            target.setSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        @Override
        public void run() {
            setVisible(true);
        }
    }
}
