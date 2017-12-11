package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.PlayerController;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.TestJet;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.GameObjects.MovingObject;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class GameState {

    private static final int MAX_COLLISION_ITERATIONS = 10;
    private final Controller playerInput = new PlayerController();
    private AbstractJet playerJet = new TestJet(playerInput);

    protected Collection<GameObject> objects = new ArrayList<>();
    protected Collection<Touchable> staticObjects = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    /** a protector that should protecc the {@code objects} list (and possibly other   */
    private Semaphore gameChangeGuard = new Semaphore(1);

    private int debugVariable = 0;

    protected void buildScene() {
        objects.add(new TestJet(playerInput));
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    /**
     * update the physics of all game objects and check for collisions
     * @param deltaTime time since last renderloop
     */
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop(float deltaTime) throws InterruptedException {
        // update positions with respect to collisions
        objects.forEach((gameObject) -> gameObject.preUpdate(deltaTime));

        if (Settings.UNIT_COLLISION) {
            int remainingLoops = MAX_COLLISION_ITERATIONS;
            Integer[] collisions = {0};
            do {
                remainingLoops--;
                collisions[0] = 0;
                checkUnitCollisions(collisions);

            // loop if
                // (1) recursive collision is enabled,
                // (2) we did not reach the maximum number of loops and
                // (3) there have been changes in the last loop
            } while (Settings.RECURSIVE_COLLISION && remainingLoops > 0 && collisions[0] > 0);
        }

        gameChangeGuard.acquire();
        objects.forEach(obj -> obj.update(deltaTime));
        gameChangeGuard.release();
    }

    /** checks the collisions of all objects and ensures that collisions[0] > 0 iff there has been a collision */
    private void checkUnitCollisions(Integer[] collisions) {
        getIntersectingPairs().parallelStream()
                .filter(GameState::checkPair)
                .forEach(p -> {
                    collisions[0]++; // race conditions don't matter, as long as collisions[0] > 0
                    applyCollisions(p);
                });
    }

    /** calls {@link MovingObject#applyCollision()} on each object of p for which it is valid */
    private void applyCollisions(Pair<Touchable, MovingObject> p) {
        p.right.applyCollision();
        if (p.left instanceof MovingObject) {
            ((MovingObject) p.left).applyCollision();
        }
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingObject#applyCollision()}
     * @param p a pair of objects that may have collided.
     * @return true if this pair collided:
     * if this method returns false, then these objects do not collide and are not changed as a result.
     * if this method returns true, then these objects do collide and have this collision stored.
     * The collision can be calculated by {@link MovingObject#applyCollision()} and applied by {@link Updatable#update(float)}
     */
    private static boolean checkPair(Pair<Touchable, MovingObject> p) {
        Touchable either = p.left;
        MovingObject moving = p.right;

        boolean change = moving.checkCollisionWith(either);
        if (either instanceof MovingObject) {
            return change || ((MovingObject) either).checkCollisionWith(moving);
        }
        return change;
    }

    /** TODO efficient implementation
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingObject>> getIntersectingPairs() {
        final Collection<Pair<Touchable, MovingObject>> result = new ArrayList<>();

        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        objects.parallelStream().forEach(obj -> {
            objects.stream()
                    // only other objects
                    .filter(o -> obj != o)
                    .forEach(other -> result.add(new Pair<>(other, obj)));
            staticObjects
                    .forEach(other -> result.add(new Pair<>(other, obj)));
        });

        Toolbox.printSpamless("created " + result.size() + " combinations");
        return result;
    }

    public void setLights(GL2 gl) {
        lights.forEach((pointLight) -> gl.setLight(pointLight.left, pointLight.right));
    }

    /**
     * draw all objects of the game
     * @param gl
     */
    public void drawObjects(GL2 gl) {
        gl.pushMatrix();
        gl.rotate(DirVector.Z, (debugVariable++) / 40f);
        Toolbox.drawAxisFrame(gl);
        gl.popMatrix();

        // static objects can not have interference
        staticObjects.forEach(d -> d.draw(gl));

        try {
            gameChangeGuard.acquire();
            objects.forEach(d -> d.draw(gl));
            gameChangeGuard.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
            gl.popAll();
            //TODO how to handle GL object?
        }
    }

    public void drawParticles(GL2 gl){
        particles.forEach(gl::draw);
    }

    /**
     * update the position of the particles.
     * should be called every renderloop
     * @param elapsedSeconds time since last renderframe
     */
    public void updateParticles(float elapsedSeconds) {
        particles.forEach(p -> p.updateRender(elapsedSeconds));
    }

    public AbstractJet getPlayer() {
        return playerJet;
    }
}
