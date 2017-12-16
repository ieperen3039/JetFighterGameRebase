package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.PlayerPCControllerAbsolute;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.GameObjects.MovingObject;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class GameState {

    private static final int MAX_COLLISION_ITERATIONS = 5;
    private final Controller playerInput = new PlayerPCControllerAbsolute();
    private AbstractJet playerJet = new PlayerJet(playerInput);

    protected Collection<GameObject> dynamicObjects = new ArrayList<>();
    protected Collection<Touchable> staticObjects = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    /** a protector that should protecc the {@code objects} list (and possibly other   */
    private Semaphore gameChangeGuard = new Semaphore(1);

    private final GameTimer time = new GameTimer();

    protected void buildScene() {
        dynamicObjects.add(playerJet);
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    /**
     * update the physics of all game objects and check for collisions
     */
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() throws InterruptedException {
        float currentTime = time.getGameTime().current();

        // update positions with respect to collisions
        dynamicObjects.forEach((gameObject) -> gameObject.preUpdate(currentTime));

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
        dynamicObjects.forEach(obj -> obj.update(currentTime, time.getGameTime().difference()));
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
        dynamicObjects.parallelStream().forEach(obj -> {
            dynamicObjects.stream()
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
     */
    public void drawObjects(GL2 gl) {
        Toolbox.drawAxisFrame(gl);

        // static objects can not have interference
        staticObjects.forEach(d -> d.draw(gl));

        try {
            gameChangeGuard.acquire();
            dynamicObjects.forEach(d -> d.draw(gl, time.getRenderTime().current()));
            gameChangeGuard.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
            gl.popAll();
        }
    }

    public void drawParticles(GL2 gl){
        particles.forEach(gl::draw);
    }

    /**
     * update the position of the particles.
     * should be called every renderloop
     */
    public void updateParticles() {
        particles.forEach(p -> p.updateRender(time.getRenderTime().difference()));
    }

    public AbstractJet getPlayer() {
        return playerJet;
    }

    /**
     * a class that harbors a gameloop timer and a render timer, upon retrieving either of these timers, they are updated
     * with a modifiable in-game time.
     */
    private class GameTimer {

        /** allows the game to slow down or speed up. with timeMultiplier = 2, the game goes twice as fast */
        private float timeMultiplier = 1f;
        /** in-game seconds since creating this gametimer */
        private float currentInGameTime;

        private long lastMark;

        private final TrackedFloat gameTime;
        private final TrackedFloat renderTime;

        public GameTimer() {
            currentInGameTime = 0f;
            gameTime = new TrackedFloat(0f);
            renderTime = new TrackedFloat(0f);
            lastMark = System.currentTimeMillis();
        }

        public TrackedFloat getGameTime(){
            updateTimer();
            gameTime.update(currentInGameTime);
            return gameTime;
        }

        public TrackedFloat getRenderTime(){
            updateTimer();
            renderTime.update(currentInGameTime - Settings.RENDER_DELAY);
            return renderTime;
        }

        public float time(){
            updateTimer();
            return currentInGameTime;
        }

        /** may be called anytime */
        private void updateTimer(){
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastMark) / 1000f;
            lastMark = currentTime;
            currentInGameTime += timeMultiplier * deltaTime;
        }

        /**
         * @param multiplier time will move {@code multiplier} as fast
         */
        public void setGameTimeMultiplier(float multiplier) {
            timeMultiplier = multiplier;
        }
    }
}
