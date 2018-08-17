package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Camera.CameraManager;
import nl.NG.Jetfightergame.Controllers.ActionButtonHandler;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.PowerupDisplay;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.RaceProgressDisplay;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.ClientControl;
import nl.NG.Jetfightergame.ServerNetwork.EnvironmentClass;
import nl.NG.Jetfightergame.ServerNetwork.JetFighterServer;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.StreamPipe;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.FollowingCamera;
import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.PointCenteredCamera;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame {
    private static final boolean USE_SOCKET_FOR_OFFLINE = false;
    private final ActionButtonHandler actionHandler;
    private GLFWWindow window;
    private GameMode currentGameMode;
    private AbstractGameLoop renderLoop;
    private Collection<AbstractGameLoop> otherLoops = new HashSet<>();

    private ClientConnection connection;
    private final CameraManager camera;

    /**
     * Shows a splash screen, and creates a window in which the game runs
     * @param makeLocalServer if true, a new server will be created and connected to on this machine.
     */
    public JetFighterGame(boolean makeLocalServer) throws Exception {
        this.window = new GLFWWindow(ServerSettings.GAME_NAME, 1600, 900, true);

        GeneralShapes.init(true);
        CustomJetShapes.init(true);
        MouseTracker.getInstance().setGameModeDecision(() -> currentGameMode != GameMode.MENU_MODE);
        MouseTracker.getInstance().listenTo(window);
        KeyTracker.getInstance().listenTo(window);

//        new SoundEngine();
//        Sounds.initAll(); // TODO also enable checkALError() in exitGame()

        Splash splash = new Splash();
        splash.run();

        try {
            OutputStream sendChannel;
            InputStream receiveChannel;

            if (makeLocalServer) {
                Logger.INFO.print("Creating new local server");

                if (USE_SOCKET_FOR_OFFLINE) {
                    JetFighterServer server = new JetFighterServer(EnvironmentClass.ISLAND_MAP);
                    new Thread(server::listenForHost).start();

                    Socket client = new Socket(InetAddress.getLocalHost(), ServerSettings.SERVER_PORT);
                    sendChannel = client.getOutputStream();
                    receiveChannel = client.getInputStream();
                    server.listenInThread(true);

                } else {
                    StreamPipe serverToClient = new StreamPipe(1024);
                    StreamPipe clientToServer = new StreamPipe(256);

                    InputStream serverReceive = serverToClient.getInputStream();
                    OutputStream serverSend = clientToServer.getOutputStream();

                    JetFighterServer server = new JetFighterServer(EnvironmentClass.ISLAND_MAP);
                    new Thread(() -> server.shortConnect(serverReceive, serverSend, true)).start();
                    server.listenInThread(true);

                    AbstractGameLoop serverLoop = server.getRunnable();
                    serverLoop.setDaemon(true);
                    serverLoop.start();

                    sendChannel = serverToClient.getOutputStream();
                    receiveChannel = clientToServer.getInputStream();
                }

            } else {
                Logger.INFO.print("Searching local server");
                Socket socket = new Socket(InetAddress.getLocalHost(), ServerSettings.SERVER_PORT);
                sendChannel = socket.getOutputStream();
                receiveChannel = socket.getInputStream();
            }

            String playerName = "TheLegend" + Toolbox.random.nextInt(1000);
            connection = new ClientConnection(playerName, sendChannel, receiveChannel);
            otherLoops.add(connection);
            actionHandler = new ActionButtonHandler(this, connection);

            ClientControl player = connection;
            Environment gameState = connection.getWorld();
            AbstractJet playerJet = player.jet();
            Logger.DEBUG.print("Received " + playerJet + " from the server");

            camera = new CameraManager();
            Consumer<ScreenOverlay.Painter> hud = new GravityHud(playerJet, camera)
                    .andThen(new PowerupDisplay(player))
                    .andThen(connection.countDownGui())
                    .andThen(new RaceProgressDisplay(connection::getRaceProgress, connection.getTimer()));

            renderLoop = new JetFighterRenderer(
                    this, gameState, window, camera, player.getInputControl(), hud
            );

            camera.switchTo(PointCenteredCamera, new PosVector(-5, 4, 2), playerJet, DirVector.zVector());
            connection.listenInThread(true);

            // set currentGameMode and engine.isPaused
            setMenuMode();
            Logger.printOnline(() -> playerJet.getPosition().toString());

        } finally {
            // remove splash frame
            splash.dispose();
        }

        // reclaim all space used for initialisation
        System.gc();
        Logger.INFO.print("Initialisation complete\n");
    }

    public void setMenuMode() {
        currentGameMode = GameMode.MENU_MODE;
        window.freePointer();
        otherLoops.forEach(AbstractGameLoop::pause);
    }

    public void setPlayMode() {
        currentGameMode = GameMode.PLAY_MODE;
        window.capturePointer();
        camera.switchTo(FollowingCamera);
        otherLoops.forEach(AbstractGameLoop::unPause);
    }

    public GameTimer getTimer() {
        return connection.getTimer();
    }

    /**
     * Start closing and cleaning everything
     */
    public void cleanUp() {
        Mesh.cleanAll();
        AudioFile.cleanAll();
        SoundEngine.closeDevices();
        actionHandler.cleanUp();
    }

    /**
     * create a thread for everyone who wants one, open the main window and start the game Rendering must happen in the
     * main thread.
     */
    public void root() {
        window.open();

        Logger.DEBUG.print("Starting " + otherLoops);
        otherLoops.forEach(Thread::start);

        try {
            renderLoop.unPause();
            renderLoop.run(); // blocks until the client is quit

            for (AbstractGameLoop gameLoop : otherLoops) {
                Logger.DEBUG.print("Waiting for " + gameLoop + " to stop");
                gameLoop.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
            renderLoop.interrupt();
            otherLoops.forEach(Thread::interrupt);

        } finally {
            Toolbox.checkGLError();
//            Toolbox.checkALError();
            this.cleanUp();
            window.cleanup();
        }

        Logger.DEBUG.print("Game has stopped! Bye ~");
        // Finish execution
    }

    /** tells the gameloops to stop */
    public void exitGame() {
        // wait for possible printing
        Toolbox.waitFor(1);

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
        return getCurrentGameMode() == GameMode.MENU_MODE;
    }

    public enum GameMode {
        PLAY_MODE, MENU_MODE
    }


    public static void main(String... argArray) throws Exception {
        List<String> args = Arrays.asList(argArray);
        boolean makeLocalServer = args.contains("-local");
        new JetFighterGame(makeLocalServer).root();
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
         * @param image some image
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
