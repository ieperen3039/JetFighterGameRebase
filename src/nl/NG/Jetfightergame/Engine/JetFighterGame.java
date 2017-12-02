package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.*;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.TestJet;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.GameObjects.MovingObject;
import nl.NG.Jetfightergame.GameObjects.Particles.AbstractParticle;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.PosVector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 *         a class that manages all game objects, and houses both the rendering- and the gameloop
 */
public class JetFighterGame extends GLFWGameEngine implements TrackerKeyListener {

    private final Controller playerInput = new PlayerController();
    protected AbstractJet playerJet = new TestJet(gameLoop, playerInput);

    protected Collection<GameObject> objects = new LinkedList<>();
    protected Collection<Touchable> staticObjects = new LinkedList<>();
    protected Collection<AbstractParticle> particles = new LinkedList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new LinkedList<>();

    /**
     * openWindow the game by creating a frame based on this engine
     */
    public JetFighterGame() throws Exception {
        super();
        Splash splash = new Splash();
        splash.run();

        try {
            KeyTracker keyTracker = KeyTracker.getInstance();
            keyTracker.addKeyListener(this);
            keyTracker.addKey(GLFW_KEY_ESCAPE);
            keyTracker.addKey(GLFW_KEY_EQUAL);
            keyTracker.addKey(GLFW_KEY_F11);

            final BooleanSupplier gameMode = () -> !this.isPaused();
            MouseTracker.getInstance().setMenuModeDecision(gameMode);

            gameLoop = new JetFighterRunner(this);
            renderLoop = new JetFighterRenderer(window, camera, this);

            // set currentGameMode and engine.isPaused
            setMenuMode();

            // playerJet = new PlayerJet(gameLoop, playerInput);
            buildScene();

        } catch (Exception e) { // prevent game from freezing upon crashing
            splash.dispose();
            throw e;
        }

        // remove splash frame
        splash.dispose();
        // reclaim all space used for initialisation
        System.gc();
    }

    public static void main(String args[]) throws Exception {
        new JetFighterGame().startGame();
    }

    public void updateGameLoop(float deltaTime) {
        // update positions with respect to collisions
        objects.forEach((gameObject) -> gameObject.preUpdate(deltaTime));
        if (Settings.UNIT_COLLISION) {
            getIntersectingPairs().parallelStream()
                    .forEach(JetFighterGame::checkPair);
        }
        objects.forEach(MovingObject::postUpdate);
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingObject#postUpdate()}
     * @param p a pair of objects that may have collided.
     */
    private static void checkPair(Pair<Touchable, MovingObject> p) {
        Touchable either = p.left;
        MovingObject moving = p.right;

        moving.checkCollisionWith(either);
        if (either instanceof MovingObject) ((MovingObject) either).checkCollisionWith(moving);
    }

    protected void buildScene() {
        objects.add(new TestJet(gameLoop, playerInput));
    }

    /** TODO efficient implementation
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingObject>> getIntersectingPairs() {
        final Collection<Pair<Touchable, MovingObject>> result = new LinkedList<>();

        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        objects.parallelStream().forEach(obj -> {
            objects.stream()
                    .filter(o -> obj != o) // only other objects
                    .forEach(other -> result.add(new Pair<>(other, obj)));
            staticObjects
                    .forEach(other -> result.add(new Pair<>(other, obj)));
        });

        Toolbox.printSpamless("created " + result.size() + " combinations");
        return result;
    }

    public void drawObjects(GL2 gl) {
        lights.forEach((pointLight) -> gl.setLight(pointLight.left, pointLight.right));

        staticObjects.forEach(d -> d.draw(gl));
        objects.forEach(d -> d.draw(gl));
    }

    public void drawParticles(GL2 gl){
        particles.forEach(gl::draw);
    }

    public void updateParticles(float elapsedSeconds) {
        particles.forEach(p -> p.updateRender(elapsedSeconds));
    }

    @Override
    public void cleanup() {
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
                if (isPaused()) exitGame();
                else setMenuMode();
                break;
            case GLFW_KEY_F11:
                Toolbox.print("Switching fullscreen");
                window.setFullScreen();
                break;
            case GLFW_KEY_EQUAL:
                gameLoop.resetTPSCounter();
                renderLoop.resetTPSCounter();
                break;
        }
    }

    @Override
    public AbstractJet getPlayer() {
        return playerJet;
    }

    /**
     * a splash image that can be shown and disposed.
     */
    private class Splash extends Frame implements Runnable {
        public Splash() {
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
