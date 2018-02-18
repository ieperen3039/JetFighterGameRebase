package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState.Environment;
import nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager.Worlds;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.EnemyFlyingTarget;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * This is a spaceless, timeless dimension, and serves as main menu.
 * @author Geert van Ieperen
 * created on 13-2-2018.
 */
public class Process592 implements Environment {

    private static final int SCALE = 50;
    private static final float OFFSET = 0.1f;
    private static final float SPACE = SCALE/2;
    private final Player player;
    private final ArrayList<Particle> particles;

    private GameTimer time = new GameTimer(); // I know, time does not exist, but it is for user experience.

    /** how much the player looks up */
    private float phi = 0;
    /** how much the player rolls left */
    private float theta = 0;

    private float phiFactor = 10f;
    private float thetaFactor = -20f;

    private MenuPanel[] currentItems;
    private int selection;

    /**
     * @param player the player
     * @param worldSelector Consumer of worlds
     */
    public Process592(Player player, Consumer<Worlds> worldSelector) {
        this.player = player;
        player.jet().set();
        particles = new ArrayList<>();
        currentItems = worldSelectionMenu(worldSelector);
    }

    @Override
    public void updateGameLoop() {
    }

    @Override
    public void setLights(GL2 gl) {
    }

    @Override
    public void drawObjects(GL2 gl) {
        glDisable(GL_CULL_FACE);
        player.jet().draw(gl);

        gl.pushMatrix();
        {
            gl.rotate(DirVector.yVector(), phi);
            gl.rotate(DirVector.xVector(), theta);
            gl.translate(SPACE, 0, 0);
            gl.scale(SCALE);
            // draw the menu panels
            drawPanels(gl, currentItems.length);
            // activate the menu panel if the player clicks
        }
        gl.popMatrix();

        readControllerInput();
    }

    /**
     * this method uses the default phisics and controller settings,
     * but is not compatible with a simple setting of panel raytracing
     * @param gl
     */
    private void updateAndDrawPlayer(GL2 gl) {
        final AbstractJet jet = player.jet();
        jet.applyPhysics(DirVector.zeroVector(), 0.1f);
        jet.update(0.1f);
        jet.set(PosVector.zeroVector());
        player.jet().draw(gl);
    }

    private void drawPanels(GL2 gl, int parts) {
        float t = (float) ((2 * Math.PI) / parts);

        selection = -1;
        for (int i = 0; i < parts; i++) {
            gl.setMaterial(Material.GLOWING, Color4f.GREEN);
            gl.pushMatrix();
            {
                gl.translate(0, OFFSET, OFFSET);

                final MenuPanel panel = currentItems[i];
                readSelected(gl, panel, i);
                gl.draw(panel.getShape());
            }
            gl.popMatrix();

            gl.rotate(DirVector.xVector(), t);
        }
    }

    private void readSelected(GL2 gl, MenuPanel panel, int i) {

        final Iterator<PosVector> p = panel.getBorder()
                .map(gl::getPosition)
                .iterator();

        // we assume that a panel has 4 points, this may become a problem
        if (MenuPanel.encapsulates(p.next(), p.next(), p.next(), p.next())) {
            gl.setMaterial(Material.GLOWING, Color4f.RED);
            selection = i;
        } else {
            selection = -1;
        }
    }

    private void readControllerInput() {
        // read controller input
        final Controller input = player.getInput();

        final Float deltaTime = time.getGameTime().difference();

        phi += input.pitch() * Settings.PITCH_MODIFIER * phiFactor * deltaTime;
        theta += input.roll() * Settings.ROLL_MODIFIER * thetaFactor * deltaTime;

        if (input.primaryFire() && (selection >= 0)) currentItems[selection].run();
    }

    @Override
    public void drawParticles(GL2 gl) {
        particles.forEach(p -> p.draw(gl));
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    @Override
    public void cleanUp() {
        particles.clear();
    }

    @Override
    public void addEntity(MovingEntity entity) {
        // maybe a bit pessimistic
        throw new UnsupportedOperationException();
    }

    @Override
    public void addParticles(Collection<Particle> newParticles) {
        particles.addAll(newParticles);
    }

    @Override
    public HUDTargetable getHUDTarget(MovingEntity entity) {
        return new EnemyFlyingTarget(entity, PosVector::zeroVector);
    }

    /**
     * creates menuPanels for selecting worlds
     * @param sceneSelector switches the current world to the given world
     * @return a new menu with all values defined in {@link nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager.Worlds}
     */
    private static MenuPanel[] worldSelectionMenu(Consumer<Worlds> sceneSelector) {
        final Worlds[] allScenes = Worlds.values();
        final int numberOfScenes = allScenes.length;
        MenuPanel[] main = new MenuPanel[numberOfScenes];

        for (int i = 0; i < numberOfScenes; i++) {
            Worlds scene = allScenes[i];
            main[i] = MenuPanel.get(numberOfScenes, () -> sceneSelector.accept(scene));
        }

        return main;
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.2f, 0.2f, 0.2f, 0f);
    }

    protected static class MenuPanel implements Runnable {
        private static final float INNER = 0.3f;
        private static Map<Integer, Shape> cache = new HashMap<>();

        private final Runnable action;
        private final int size;
        private final Shape shape;

        private MenuPanel(Shape parts, Runnable action, int n) {
            this.shape = parts;
            this.action = action;
            size = n;
        }

        /**
         * @param parts how many parts there are in total. If you want 1/6th of a circle, parts = 6
         * @param action the action it executes when the player manages to click it
         * @return a panel with its shape on the yz plane, that extends to z=1, with front towards positive x.
         */
        @SuppressWarnings("SuspiciousNameCombination")
        public static MenuPanel get(int parts, Runnable action){
            if (cache.containsKey(parts)) return new MenuPanel(cache.get(parts), action, parts);

            CustomShape frame = new CustomShape(new PosVector(-1, 0, 0));

            Iterator<PosVector> i = getBorder(parts).iterator();

            frame.addQuad(
                    i.next(), i.next(), i.next(), i.next()
            );

            Shape shape = frame.wrapUp();
            cache.put(parts, shape);

            return new MenuPanel(shape, action, parts);
        }

        @Override
        public void run() {
            action.run();
        }

        /**
         * vectors must be supplied in rotational order
         * @return true iff the given vertices encapsule the X axis
         */
        protected static boolean encapsulates(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4) {
            boolean mark = sign(v1, v2);
            return (mark == sign(v2, v3)) && (mark == sign(v3, v4)) && (mark == sign(v4, v1));
        }

        private static boolean sign(Vector3f alpha, Vector3f beta) {
            return ((-beta.y * (alpha.z - beta.z)) + ((alpha.y - beta.y) * beta.z)) < 0.0f;
        }

        public Shape getShape() {
            return shape;
        }

        public Stream<PosVector> getBorder() {
            return getBorder(size);
        }

        private static Stream<PosVector> getBorder(int parts) {

            double t = (2 * Math.PI) / parts;
            float y = (float) Math.cos(t);
            float x = (float) Math.sin(t);

            PosVector[] list = {new PosVector(0, 0, 1f),
                    new PosVector(0, 0, INNER),
                    new PosVector(0, x * INNER, y * INNER),
                    new PosVector(0, x, y)
            };

            return Arrays.stream(list);
        }
    }
}
