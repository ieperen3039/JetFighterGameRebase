package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Controllers.ActionButtonHandler;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.PowerupDisplay;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.*;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.StreamPipe;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.PointCenteredCamera;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine {
    private static final boolean USE_SOCKET_FOR_OFFLINE = false;
    private final ActionButtonHandler actionHandler;
    private AbstractGameLoop renderLoop;
    private Collection<AbstractGameLoop> otherLoops = new HashSet<>();

    private ClientConnection connection;

    /** Shows a splash screen, and creates a window in which the game runs */
    public JetFighterGame() throws Exception {
        super();
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

            if (ClientSettings.LOCAL_SERVER) {
                Logger.INFO.print("Creating new local server");

                if (USE_SOCKET_FOR_OFFLINE) {
                    Socket client = new Socket();
                    otherLoops.add(JetFighterServer.createOfflineServer(EnvironmentClass.ISLAND_MAP, client));
                    sendChannel = client.getOutputStream();
                    receiveChannel = client.getInputStream();

                } else {
                    StreamPipe serverToClient = new StreamPipe(1024);
                    StreamPipe clientToServer = new StreamPipe(126);

                    InputStream serverReceive = serverToClient.getInputStream();
                    OutputStream serverSend = clientToServer.getOutputStream();

                    JetFighterServer server = new JetFighterServer(EnvironmentClass.ISLAND_MAP);
                    new Thread(() -> server.shortConnect(serverReceive, serverSend, true)).start();
                    otherLoops.add(server.getRunnable());

                    sendChannel = serverToClient.getOutputStream();
                    receiveChannel = clientToServer.getInputStream();
                }

            } else {
                Logger.INFO.print("Searching local server");
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ServerSettings.SERVER_PORT));
                sendChannel = socket.getOutputStream();
                receiveChannel = socket.getInputStream();
            }

            connection = new ClientConnection("TheLegend27", sendChannel, receiveChannel);
            otherLoops.add(connection);
            actionHandler = new ActionButtonHandler(this, connection);

            ClientControl player = connection;
            Environment gameState = connection.getWorld();
            AbstractJet playerJet = player.jet();
            Logger.DEBUG.print("Received " + playerJet + " from the server");

            Consumer<ScreenOverlay.Painter> hud = new GravityHud(playerJet, camera)
                    .andThen(new PowerupDisplay(player))
                    .andThen(connection.countDownGui());

            renderLoop = new JetFighterRenderer(
                    this, gameState, window, camera, player.getInputControl(), hud
            );

            camera.switchTo(PointCenteredCamera, new PosVector(-5, 4, 2), playerJet, DirVector.zVector());

            new Thread(connection::listen).start();

            // set currentGameMode and engine.isPaused
            setMenuMode();

        } finally {
            // remove splash frame
            splash.dispose();
        }

        // reclaim all space used for initialisation
        System.gc();
        Logger.INFO.print("Initialisation complete\n");
    }

    @Override
    protected AbstractGameLoop renderingLoop() {
        return renderLoop;
    }

    @Override
    protected Collection<AbstractGameLoop> secondaryGameLoops() {
        return Collections.unmodifiableCollection(otherLoops);
    }

    public static void main(String... args) {
        try {
            new JetFighterGame().root();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(1);
    }

    @Override
    public void setMenuMode() {
        connection.getTimer().pause();
        connection.sendCommand(MessageType.PAUSE_GAME);
        super.setMenuMode();
    }

    public void setPlayMode() {
        connection.getTimer().unPause();
        connection.sendCommand(MessageType.UNPAUSE_GAME);
        if (ClientSettings.SPECTATOR_MODE) {
            super.setSpectatorMode();
        } else {
            super.setPlayMode();
        }
    }

    @Override
    public GameTimer getTimer() {
        return connection.getTimer();
    }

    @Override
    public void cleanUp() {
        Mesh.cleanAll();
        AudioFile.cleanAll();
        SoundEngine.closeDevices();
        actionHandler.cleanUp();
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
