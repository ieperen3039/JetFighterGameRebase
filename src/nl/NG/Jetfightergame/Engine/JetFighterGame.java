package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.FollowingCamera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Controllers.*;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.GameObjects.MovingObject;
import nl.NG.Jetfightergame.GameObjects.Particles.AbstractParticle;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Scenarios.SimplexCave;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {

    private GameMode currentGameMode;

    private final Controller playerInput = new PlayerController();
    private final AbstractJet playerJet = new PlayerJet(gameLoop, playerInput);

    private Collection<GameObject> objects = new LinkedList<>();
    private Collection<Touchable> staticObjects = new LinkedList<>();
    private Collection<AbstractParticle> particles = new LinkedList<>();

    /**
     * openWindow the game by creating a frame based on this engine
     */
    private JetFighterGame() throws Exception {
        super();
        Frame splash = new Splash();
        splash.setVisible(true);

        try {
            KeyTracker.getInstance().addKeyListener(this);

            final BooleanSupplier gameMode = () -> this.getCurrentGameMode() == GameMode.PLAY_MODE;
            MouseTracker.getInstance().setMenuModeDecision(gameMode);

            gameLoop = new JetFighterRunner(this);
            renderLoop = new JetFighterRenderer(window, camera);

            // set currentGameMode and engine.isPaused
            setMenuMode();

            buildScene();

        } catch (Exception e) { // prevent game from freezing upon crashing
            splash.dispose();
            throw e;
        }

        // remove splash frame
        splash.dispose();
    }

    public static void main(String args[]) throws Exception {
        new JetFighterGame();
    }

    protected void updateGameLoop(float deltaTime) {
        // update positions with respect to collisions
        objects.forEach((gameObject) -> gameObject.preUpdate(deltaTime));
        if (Settings.UNIT_COLLISION || !Settings.DEBUG) {
            Collection<Pair<Touchable, Touchable>> candidates = getIntersectingPairs();
            candidates.parallelStream().forEach(p -> {
                p.left.checkCollisionWith(p.right);
                p.right.checkCollisionWith(p.left);
            });
        }
        objects.forEach(MovingObject::postUpdate);
    }

    private void buildScene() {
        staticObjects.add(new SimplexCave());
    }

    /**
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, Touchable>> getIntersectingPairs() {
        final Collection<Pair<Touchable, Touchable>> result = new LinkedList<>();

        // Naive solution: return all n^2 options
        Collection<Touchable> touchables = new LinkedList<>();
        touchables.addAll(objects);
        touchables.addAll(staticObjects);
        touchables.forEach(a -> touchables.forEach(b -> {
            if (a != b) {
                final Pair<Touchable, Touchable> p = new Pair<>(a, b);
                result.add(p);
                Toolbox.printSpamless(p);
            }
        }));
        Toolbox.printSpamless("created " + result.size() + " combinations");
        return result;
    }

    @Override
    public void exitGame() {
        Toolbox.print("Stopping...");
        super.exitGame();
    }

    public GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public void setMenuMode() {
        // TODO set cursor visibility
        this.currentGameMode = GameMode.MENU_MODE;
        camera = new PointCenteredCamera(playerJet.getPosition(), DirVector.Z, 1, 1);
        gameLoop.pause();
    }

    public void setPlayMode() {
        // TODO set cursor visibility
        this.currentGameMode = GameMode.PLAY_MODE;
        camera = new FollowingCamera(camera.getEye(), getPlayerJet());
        gameLoop.unPause();
    }

    /**
     * basic keybindings.
     * Should be removed in later stage
     * @param key
     */
    @Override
    public void keyPressed(int key) {
        // quit when Esc is pressed
        switch (key) {
            case KeyEvent.VK_ESCAPE:
                if (currentGameMode == GameMode.MENU_MODE) exitGame();
                else setMenuMode();
                break;
            case KeyEvent.VK_F11:
                Toolbox.print("Switching fullscreen");
                window.setFullScreen();
                break;
            case KeyEvent.VK_EQUALS:
                gameLoop.resetTPSCounter();
                renderLoop.resetTPSCounter();
                break;
        }
    }

    public AbstractJet getPlayerJet() {
        return playerJet;
    }

    public enum GameMode {
        PLAY_MODE, MENU_MODE
    }

    /**
     * a splash image that can be shown and disposed.
     */
    private class Splash extends Frame {
        private Splash() {
            // TODO better splash image
            final String path = "res/Pictures/SplashImage.png";

            Frame splash = new Frame("Loading Jet Fighter");

            try {
                final BufferedImage splashImage = ImageIO.read(new File(path));
                setImage(splash, splashImage);
            } catch (Exception e) {
                System.err.println("Could not find splash image!");
                e.printStackTrace();
                splash.setSize(new Dimension(500, 300));
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Point centerPoint = ge.getCenterPoint();

            int dx = centerPoint.x - splash.getWidth() / 2;
            int dy = centerPoint.y - splash.getHeight() / 2;

            splash.setLocation(dx, dy);
            splash.setUndecorated(true);
            splash.setBackground(Color.WHITE);
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
    }
}
