package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.JetFighterServer;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromFile;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Toolbox;
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
import java.util.function.Function;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.PointCenteredCamera;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {
    protected AbstractGameLoop renderLoop;

    private Environment environment;
    private Collection<AbstractGameLoop> otherLoops = new HashSet<>();

    private GameTimer globalGameTimer;

    private final Player player;
    private ClientConnection connection;

    /** Shows a splash screen, and creates a window in which the game runs */
    public JetFighterGame() throws Exception {
        super();

        Splash splash = new Splash();
        splash.run();

        try {
            globalGameTimer = new GameTimer();
            Socket socket = new Socket();

            Function<GameTimer, Environment> worldFactory = PlayerJetLaboratory::new;

            ShapeFromFile.init(true);
            GeneralShapes.init(true);

            environment = worldFactory.apply(globalGameTimer);
            otherLoops.add(new PhysicsLoop(environment));

            if (ClientSettings.LOCAL_SERVER) {
                Toolbox.print("Creating new local server");
                // a second (new) environment is created, as the server runs separately from the client
                otherLoops.add(JetFighterServer.createOfflineServer(worldFactory, socket));

            } else {
                Toolbox.print("Searching local server");
                socket.connect(new InetSocketAddress(ServerSettings.SERVER_PORT));
            }

            connection = new ClientConnection((ex) -> exitGame(), playerInput, socket, environment);

            otherLoops.add(connection);
            AbstractJet playerJet = connection.getPlayer();
            Toolbox.print("Received " + playerJet + " from the server");

            player = new Player(playerInput, playerJet);
            environment.buildScene(connection, ClientSettings.COLLISION_DETECTION_LEVEL, false);
            environment.addEntity(playerJet);

            KeyTracker keyTracker = KeyTracker.getInstance();
            keyTracker.addKeyListener(this);

//        new SoundEngine();
//        Sounds.initAll(); // TODO also enable checkALError() in exitGame()

            ScreenOverlay.initialize(() -> currentGameMode == GameMode.MENU_MODE);

            renderLoop = new JetFighterRenderer(
                    this, environment, window, camera, playerInput, playerJet
            );

            camera.switchTo(PointCenteredCamera, new PosVector(3, -3, 2), playerJet, DirVector.zVector());

            environment.updateGameLoop();
            new Thread(connection::listen).start();

            // set currentGameMode and engine.isPaused
            setMenuMode();

        } finally {
            // remove splash frame
            splash.dispose();
        }

        // reclaim all space used for initialisation
        System.gc();
        Toolbox.print("Initialisation complete\n");
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
    public void cleanUp() {
        Mesh.cleanAll();
        AudioFile.cleanAll();
        SoundEngine.closeDevices();
        KeyTracker.getInstance().removeKeyListener(this);
    }

    @Override
    public void setMenuMode() {
        connection.sendCommand(MessageType.PAUSE_GAME);
        super.setMenuMode();
    }

    public void setPlayMode() {
        connection.sendCommand(MessageType.START_GAME);
        if (ClientSettings.SPECTATOR_MODE) {
            super.setSpectatorMode();
        } else {
            super.setPlayMode();
        }
    }

    /**
     * basic keybindings.
     * Should be moved to testInstance in later stage
     * @param key
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
                Toolbox.print("Switching fullscreen");
                window.toggleFullScreen();
                break;

            case GLFW_KEY_PRINT_SCREEN:
                SimpleDateFormat ft = new SimpleDateFormat("yy-mm-dd_hh_mm_ss");
                final String name = "Screenshot_" + ft.format(new Date());

                boolean success = window.printScreen(name);
                if (success){
                    Toolbox.print("Saved screenshot as \"" + name + "\"");
                }
        }
    }

    @Override
    public AbstractJet getPlayer() {
        return player.jet();
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

    /**
     * runs the updateGameLoop of the given environment
     */
    private class PhysicsLoop extends AbstractGameLoop {
        private final Environment world;

        public PhysicsLoop(Environment environment) {
            super("Physics", ClientSettings.PHYSICS_TPS, true, (ex) -> {});
            world = environment;
        }

        @Override
        protected void update(float deltaTime) throws Exception {
            world.getTimer().updateGameTime();
            world.updateGameLoop();
        }

        @Override
        protected void cleanup() {
            world.cleanUp();
        }
    }
}
