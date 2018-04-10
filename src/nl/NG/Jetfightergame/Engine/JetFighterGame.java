package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Assets.FighterJets.BasicJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Assets.Sounds;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameLoop.JetFighterRunner;
import nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.Settings;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromMesh;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.function.BooleanSupplier;

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.PointCenteredCamera;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {
    private EnvironmentManager environment;
    protected AbstractGameLoop renderLoop;
    protected AbstractGameLoop gameLoop;
    private Collection<AbstractGameLoop> otherLoops = new ArrayList<>();
    protected SoundEngine soundEngine;

    private final GameTimer globalGameTimer;
    
    private final Player player;

    /**
     * openWindow the game by creating a frame based on this engine
     */
    public JetFighterGame() throws Exception {
        super();

        Splash splash = new Splash();
        splash.run();

        try {
            if (Settings.FIXED_DELTA_TIME) globalGameTimer = new StaticTimer(Settings.TARGET_FPS);
            else globalGameTimer = new GameTimer();

            KeyTracker keyTracker = KeyTracker.getInstance();
            keyTracker.addKeyListener(this);

            player = new Player(playerInput);
            environment = new EnvironmentManager(player, globalGameTimer);

            player.setJet(new BasicJet(playerInput, globalGameTimer, environment));

            final BooleanSupplier inGame = () -> currentGameMode == GameMode.PLAY_MODE;
            MouseTracker.getInstance().setMenuModeDecision(inGame);

            gameLoop = new JetFighterRunner(environment, e -> this.exitGame());
            otherLoops.add(gameLoop);

//            MusicProvider musicProvider = new MusicProvider(new Timer());
            soundEngine = new SoundEngine();
            Sounds.initAll();

            ScreenOverlay.initialize(() -> currentGameMode == GameMode.MENU_MODE);

            environment.init();

            renderLoop = new JetFighterRenderer(
                    this, environment, window, camera, playerInput
            );


            camera.switchTo(PointCenteredCamera, new PosVector(3, -3, 2), getPlayer(), DirVector.zVector());

            // set currentGameMode and engine.isPaused
            setMenuMode();

            ShapeFromMesh.initAll();
            GeneralShapes.initAll();
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

                if (window.printScreen(name)){
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

            setTitle("Loading " + Settings.GAME_NAME);
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
