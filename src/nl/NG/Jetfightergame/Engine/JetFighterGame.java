package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.Engine.GameLoop.JetFighterRunner;
import nl.NG.Jetfightergame.Rendering.JetFighterRenderer;
import nl.NG.Jetfightergame.Scenarios.CollisionLaboratory;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ShapeCreators.Mesh;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Sound.MusicProvider;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.BooleanSupplier;

import static nl.NG.Jetfightergame.Engine.Managers.CameraManager.CameraImpl.PointCenteredCamera;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {
    private GameState environment;

    /**
     * openWindow the game by creating a frame based on this engine
     */
    public JetFighterGame() throws Exception {
        super();
        Splash splash = new Splash();
        splash.run();

        try {
            environment = new CollisionLaboratory(playerInput);

            KeyTracker keyTracker = KeyTracker.getInstance();
            keyTracker.addKeyListener(this);

            final BooleanSupplier inGame = () -> currentGameMode == GameMode.PLAY_MODE;
            MouseTracker.getInstance().setMenuModeDecision(inGame);

            gameLoop = new JetFighterRunner(environment, e -> this.exitGame());
            MusicProvider musicProvider = new MusicProvider(new Timer());

            ScreenOverlay.initialize(() -> currentGameMode == GameMode.MENU_MODE);

            renderLoop = new JetFighterRenderer(
                    this, environment, window, camera, musicProvider, playerInput
            );

            camera.switchTo(PointCenteredCamera, new PosVector(3, -3, 2), getPlayer(), DirVector.zVector());

            environment.buildScene();

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
    }

    public static void main(String args[]) throws Exception {
        new JetFighterGame().startGame();
    }

    @Override
    public void cleanUp() {
        Mesh.cleanAll();
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
            case GLFW_KEY_EQUAL:
                gameLoop.resetTPSCounter();
                renderLoop.resetTPSCounter();
                break;
        }
    }

    @Override
    public AbstractJet getPlayer() {
        return environment.getPlayer();
    }

    /**
     * a method to facillitate testing
     * @param testEnvironment a new environment
     */
    void setGameState(GameState testEnvironment) {
        environment = testEnvironment;
    }

    /**
     * a splash image that can be shown and disposed.
     */
    private class Splash extends Frame implements Runnable {
        Splash() {
            // TODO better splash image
            final String path = "res/Pictures/SplashImage.png";

            setTitle("Loading Jet Fighter");

            try {
                final BufferedImage splashImage = ImageIO.read(new File(path));
                setImage(this, splashImage);
            } catch (Exception e) {
                System.err.println("Could not find splash image!");
                e.printStackTrace();
                setSize(new Dimension(500, 300));
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Point centerPoint = ge.getCenterPoint();

            int dx = centerPoint.x - getWidth() / 2;
            int dy = centerPoint.y - getHeight() / 2;

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
