package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.JetFighterServer;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.PointCenteredCamera;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {
    private AbstractGameLoop renderLoop;
    private Collection<AbstractGameLoop> otherLoops = new HashSet<>();

    private ClientConnection connection;

    /** Shows a splash screen, and creates a window in which the game runs */
    public JetFighterGame() throws Exception {
        super();
        GeneralShapes.init(true);

        KeyTracker.getInstance().addKeyListener(this);
        MouseTracker.getInstance().setGameModeDecision(() -> currentGameMode != GameMode.MENU_MODE);
        MouseTracker.getInstance().listenTo(window);
        KeyTracker.getInstance().listenTo(window);

//        new SoundEngine();
//        Sounds.initAll(); // TODO also enable checkALError() in exitGame()

        Splash splash = new Splash();
        splash.run();

        try {
            Socket socket = new Socket();

            // TODO get environment from the server
            Supplier<Environment> worldFactory = PlayerJetLaboratory::new;
            Environment environment = worldFactory.get();

            if (ClientSettings.LOCAL_SERVER) {
                Logger.print("Creating new local server");
                // a second (new) environment is created, as the server runs separately from the client
                otherLoops.add(JetFighterServer.createOfflineServer(worldFactory, socket));

            } else {
                Logger.print("Searching local server");
                socket.connect(new InetSocketAddress(ServerSettings.SERVER_PORT));
            }

            connection = new ClientConnection(socket, environment);
            otherLoops.add(connection);

            Player player = connection.getPlayer();
            AbstractJet playerJet = player.jet();
            Logger.print("Received " + playerJet + " from the server");

            environment.buildScene(connection, ClientSettings.COLLISION_DETECTION_LEVEL, false);
            environment.addEntity(playerJet);

            Consumer<ScreenOverlay.Painter> hud = new GravityHud(playerJet, camera);

            renderLoop = new JetFighterRenderer(
                    this, environment, window, camera, player.getInputControl(), hud
            );

            camera.switchTo(PointCenteredCamera, new PosVector(3, -3, 2), playerJet, DirVector.zVector());

            new Thread(connection::listen).start();

            // set currentGameMode and engine.isPaused
            setMenuMode();

        } finally {
            // remove splash frame
            splash.dispose();
        }

        // reclaim all space used for initialisation
        System.gc();
        Logger.print("Initialisation complete\n");
    }

    @Override
    protected AbstractGameLoop renderingLoop() {
        return renderLoop;
    }

    @Override
    protected Collection<AbstractGameLoop> secondaryGameLoops() {
        return Collections.unmodifiableCollection(otherLoops);
    }

    public static void main(String args[]) throws Exception {
        new JetFighterGame().root();
    }

    @Override
    public void setMenuMode() {
        connection.getTimer().pause();
        connection.sendCommand(MessageType.PAUSE_GAME);
        super.setMenuMode();
    }

    public void setPlayMode() {
        connection.getTimer().unPause();
        connection.sendCommand(MessageType.START_GAME);
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

    /**
     * basic keybindings.
     * Should be moved to testInstance in later stage
     */
    @Override
    public void keyPressed(int key) {
        // quit when Esc is pressed
        switch (key) {
            case GLFW_KEY_ESCAPE:
                if (currentGameMode == GameMode.MENU_MODE) exitGame();
                else setMenuMode();
                break;

            case GLFW_KEY_F11:
                Logger.print("Switching fullscreen");
                window.toggleFullScreen();
                break;

            case GLFW_KEY_PRINT_SCREEN:
                SimpleDateFormat ft = new SimpleDateFormat("yy-mm-dd_hh_mm_ss");
                final String name = "Screenshot_" + ft.format(new Date());

                boolean success = window.printScreen(name);
                if (success){
                    Logger.print("Saved screenshot as \"" + name + "\"");
                }
        }
    }

    @Override
    public void cleanUp() {
        Mesh.cleanAll();
        AudioFile.cleanAll();
        SoundEngine.closeDevices();
        KeyTracker.getInstance().removeKeyListener(this);
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
                System.err.println("Could not load splash image " + Directory.pictures.getFile(image));
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
