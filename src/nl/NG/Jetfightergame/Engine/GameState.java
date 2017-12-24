package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.EntityDefinitions.AbstractJet;
import nl.NG.Jetfightergame.EntityDefinitions.GameEntity;
import nl.NG.Jetfightergame.EntityDefinitions.MovingEntity;
import nl.NG.Jetfightergame.EntityDefinitions.Touchable;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Scenarios.TestLab;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
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

    private static final int MAX_COLLISION_ITERATIONS = 5;
    private AbstractJet playerJet;

    protected Collection<Touchable> staticEntities = new ArrayList<>();
    protected Collection<GameEntity> dynamicEntities = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    /** a protector that should protecc the {@code objects} list (and possibly other   */
    private Semaphore gameChangeGuard = new Semaphore(1);
    public final GameTimer time = new GameTimer();

    public GameState(Controller input) {
        playerJet = new PlayerJet(input);
    }

    private Extreme<Integer> collisionMax = new Extreme<>(true);

    protected void buildScene() {
        dynamicEntities.add(playerJet);
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new TestLab(100));
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    /**
     * update the physics of all game objects and check for collisions
     */
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() throws InterruptedException {
        time.updateGameTime();
        float deltaTime = time.getGameTime().difference();
        float currentTime = time.getGameTime().current();

        // update positions and apply physics // TODO external influences
        dynamicEntities.forEach((gameObject) -> gameObject.preUpdate(deltaTime, DirVector.zeroVector()));

        if (Settings.UNIT_COLLISION) {
            int remainingLoops = MAX_COLLISION_ITERATIONS;
            Integer[] collisions = {0};

            do {
                remainingLoops--;
                collisions[0] = 0;
                // as a single collision may result in a previously not-intersecting pair to collide,
                // we cannot re-use the getIntersectingPairs method nor reduce non-collisions. We should add some form
                // of caching for getIntersectingPairs, to make short-followed calls more efficient.
                checkUnitCollisions(getIntersectingPairs(), collisions);

            // loop if
                // (1) recursive collision is enabled,
                // (2) we did not reach the maximum number of loops and
                // (3) there have been changes in the last loop
            } while (Settings.RECURSIVE_COLLISION && remainingLoops > 0 && collisions[0] > 0);
        }

        gameChangeGuard.acquire();
        dynamicEntities.forEach(obj -> obj.update(currentTime, deltaTime));
        gameChangeGuard.release();
    }

    /** checks the collisions of all objects and ensures that results[0] > 0 iff there has been a collision
     * @param intersectingPairs a collection of pairs of objects that may collide.
     * @param results an array with length at least 1 to store the result
     */
    private void checkUnitCollisions(Collection<Pair<Touchable, MovingEntity>> intersectingPairs, Integer[] results) {
        intersectingPairs.parallelStream()
                .filter(GameState::checkPair)
                .forEach(p -> {
                    results[0]++; // race conditions don't matter, as long as collisions[0] > 0
                    applyCollisions(p);
                });
    }

    /** calls {@link MovingEntity#applyCollision()} on each object of p for which it is valid */
    private void applyCollisions(Pair<Touchable, MovingEntity> p) {
        p.right.applyCollision();
        if (p.left instanceof MovingEntity) {
            ((MovingEntity) p.left).applyCollision();
        }
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingEntity#applyCollision()}
     * @param p a pair of objects that may have collided.
     * @return true if this pair collided:
     * if this method returns false, then these objects do not collide and are not changed as a result.
     * if this method returns true, then these objects do collide and have this collision stored.
     * The collision can be calculated by {@link MovingEntity#applyCollision()} and applied by {@link Updatable#update(float)}
     */
    private static boolean checkPair(Pair<Touchable, MovingEntity> p) {
        Touchable either = p.left;
        MovingEntity moving = p.right;

        boolean change = moving.checkCollisionWith(either);
        if (either instanceof MovingEntity) {
            return change || ((MovingEntity) either).checkCollisionWith(moving);
        }
        return change;
    }

    /** TODO efficient implementation, Possibly move to dedicated class
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        final Collection<Pair<Touchable, MovingEntity>> result = new ArrayList<>();

        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        dynamicEntities.parallelStream().forEach(obj -> {
            dynamicEntities.stream()
                    // only other objects
                    .filter(o -> obj != o)
                    .forEach(other -> result.add(new Pair<>(other, obj)));
            staticEntities
                    .forEach(other -> result.add(new Pair<>(other, obj)));
        });

        collisionMax.updateAndPrint("Intersections", result.size(), "pairs");
        return result;
    }

    public void setLights(GL2 gl) {
        lights.forEach((pointLight) -> gl.setLight(pointLight.left, pointLight.right));
    }

    /**
     * draw all objects of the game
     */
    public void drawObjects(GL2 gl) {
        time.updateRenderTime();
        Toolbox.drawAxisFrame(gl);

        // static objects can not have interference
        staticEntities.forEach(d -> d.draw(gl));

        try {
            gameChangeGuard.acquire();
            dynamicEntities.forEach(d -> d.draw(gl, time.getRenderTime().current()));
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
    public class GameTimer {

        /** multiplication of time as effect of in-game events. with playMultiplier = 2, the game goes twice as fast. */
        private float playMultiplier = 1f;
        /** multiplication of time by the engine. Main use is for pausing. with engineMultiplier = 2, the game goes twice as fast. */
        private float engineMultiplier;

        /** in-game seconds since creating this gametimer */
        private float currentInGameTime;

        private long lastMark;

        private final TrackedFloat gameTime;
        private final TrackedFloat renderTime;

        private GameTimer() {
            currentInGameTime = 0f;
            gameTime = new TrackedFloat(0f);
            renderTime = new TrackedFloat(-Settings.RENDER_DELAY);
            lastMark = System.currentTimeMillis();
        }

        private void updateGameTime(){
            gameTime.update(currentInGameTime);
        }

        private void updateRenderTime(){
            renderTime.update(currentInGameTime - Settings.RENDER_DELAY);
        }

        public TrackedFloat getGameTime(){
            updateTimer();
            return gameTime;
        }

        public TrackedFloat getRenderTime(){
            updateTimer();
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
            currentInGameTime += deltaTime * playMultiplier * engineMultiplier;
        }

        /**
         * @param multiplier time will move {@code multiplier} times as fast
         */
        public void setGameTimeMultiplier(float multiplier) {
            playMultiplier = multiplier;
        }

        /**
         * @param multiplier time will move {@code multiplier} times as fast
         */
        public void setEngineMultiplier(float multiplier){
            engineMultiplier = multiplier;
        }
    }
}
