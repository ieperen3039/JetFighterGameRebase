package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.ClientControl;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
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
public abstract class Process592 {

    private static final int SCALE = 50;
    private static final float OFFSET = 0.1f;
    private static final float SPACE = SCALE/2;
    private final ClientControl clientControl;//TODO
    private final List<ParticleCloud> particles;

    private GameTimer time = new GameTimer(); // I know, time does not exist, but it is for user experience.

    /** how much the player looks up */
    private float phi = 0;
    /** how much the player rolls left */
    private float theta = 0;

    private static final float phiFactor = 10f;
    private static final float thetaFactor = -20f;

    private MenuPanel[] currentItems;
    private int selection;

    /**
     * @param worldSelector Consumer of worlds. Must be available and implemented
     * @param clientControl
     */
    public Process592(Consumer<Worlds> worldSelector, ClientControl clientControl) {
        particles = new ArrayList<>();
        currentItems = worldSelectionMenu(worldSelector);
        this.clientControl = clientControl;
    }

    /**
     * initialize the scene. Make sure to call Shapes.init() for all shapes you want to initialize
     * @param deposit
     * @param collisionDetLevel 0 = no collision
     * @param loadDynamic       if false, all dynamic entities are not loaded. This is required if these are managed by
     *                          a server
     */
    public void buildScene(SpawnReceiver deposit, int collisionDetLevel, boolean loadDynamic) {
        clientControl.jet().set(PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf());
    }

    public MovingEntity.State getNewSpawn() {
        return new MovingEntity.State();
    }

    /**
     * update the physics of all game objects and check for collisions
     * @param currentTime
     * @param deltaTime
         */
    public void updateGameLoop(float currentTime, float deltaTime) {
    }

    /**
     * initializes the lights of this environment in the gl environment
         */
    public void setLights(GL2 gl) {
    }

    /**
     * draw all objects of the game
         */
    public void drawObjects(GL2 gl) {
        glDisable(GL_CULL_FACE);
        clientControl.jet().draw(gl);

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
        final AbstractJet jet = clientControl.jet();
        jet.applyPhysics(DirVector.zeroVector(), 0.1f);
        jet.update(0.1f);
        jet.set(PosVector.zeroVector());
        clientControl.jet().draw(gl);
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
        final Controller input = clientControl.getInput();

        final Float deltaTime = time.getGameTime().difference();

        phi += input.pitch() * ClientSettings.PITCH_MODIFIER * phiFactor * deltaTime;
        theta += input.roll() * ClientSettings.ROLL_MODIFIER * thetaFactor * deltaTime;

        if (input.primaryFire() && (selection >= 0)) currentItems[selection].run();
    }

    public void drawParticles(float currentTime) {
        float t = time.getRenderTime().current();
        particles.removeIf(p -> p.disposeIfFaded(t));
        particles.forEach(ParticleCloud::render);
    }

    /** all entities added by the constructor or using {@link #addEntity(MovingEntity) */
    public Collection<MovingEntity> getEntities() {
        return Collections.EMPTY_SET;
    }

    /**
     * allows this object to be cleaned.
     * after calling this method, this object should not be used.
         */
    public void cleanUp() {
        particles.clear();
    }

    /**
     * adds an entity to this world
     * @param entity an entity, set in the appropriate position, not being controlled by outside resources
     * @see #getEntity(int)
         */
    public void addEntity(MovingEntity entity) {
        // maybe a bit pessimistic
        throw new UnsupportedOperationException();
    }

    /**
     * searches the entity corresponding to the given ID, or null if no such entity exists
     * @param entityID the ID number of an existing entity
     * @return the entity with the given entityID, or null if no such entity exists
         */
    public MovingEntity getEntity(int entityID) {
        return null;
    }

    /**
     * removes an entity off this world.
     * @param entity the entity to be removed
         */
    public void removeEntity(MovingEntity entity) {
        throw new UnsupportedOperationException("removeEntity");
    }

    public void addParticles(ParticleCloud newParticles) {
        particles.add(newParticles);
    }

    /**
     * creates menuPanels for selecting worlds
     * @param sceneSelector switches the current world to the given world
     * @return a new menu with all possible worlds
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

    /**
     * light of the background, alpha determines the thickness of the fog
     * @return the background-color
         */
    public Color4f fogColor(){
        return new Color4f(0.2f, 0.2f, 0.2f, 0f);
    }

    public void addEntities(Collection<? extends MovingEntity> entities) {
        entities.forEach(this::addEntity);
    }

    /**
     * adds
     * @param raceProgress
     */
    public abstract void addCheckPoints(RaceProgress raceProgress);

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

            Shape shape = frame.wrapUp(ServerSettings.RENDER_ENABLED);
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
