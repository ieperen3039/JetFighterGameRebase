package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Scenarios.ContainerCube;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class GameState {

    private AbstractJet playerJet;

    protected Collection<Touchable> staticEntities = new ArrayList<>();
    protected Collection<GameEntity> dynamicEntities = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    /** a protector that should protecc the {@code objects} list (and possibly other   */
    private Lock gameChangeGuard = new ReentrantLock();
    public final GameTimer time = new GameTimer();
    private Collection<Pair<Touchable, MovingEntity>> allEntityPairs = null;

    public GameState(Controller input) {
        playerJet = new PlayerJet(input, time.getRenderTime());
    }

    private Extreme<Integer> collisionMax = new Extreme<>(true);

    protected void buildScene() {
        dynamicEntities.add(playerJet);
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new ContainerCube(100));
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    /**
     * update the physics of all game objects and check for collisions
     */
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        time.updateGameTime();
        float previousTime = time.getGameTime().previous();
        float currentTime = time.getGameTime().current();
        float deltaTime = currentTime - previousTime;

        // update positions and apply physics
        dynamicEntities.forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));

        // check and handle collisions
        if (Settings.UNIT_COLLISION && deltaTime > 0f) {
            int remainingLoops = Settings.MAX_COLLISION_ITERATIONS;
            Integer[] collisions = {0};

            do {
                collisions[0] = 0;

                /* as a single collision may result in a previously not-intersecting pair to collide,
                 * we cannot re-use the getIntersectingPairs method nor reduce non-collisions. We should add some form
                 * of caching for getIntersectingPairs, to make short-followed calls more efficient.
                 */

                getIntersectingPairs().parallelStream()
                        .filter(GameState::checkPair)
                        .forEach(p -> {
                            collisions[0]++; // race conditions don't matter, as long as collisions[0] > 0
                            applyCollisions(p, deltaTime, previousTime);
                        });

            } while (
                    // (1) recursive collision is enabled
                    Settings.RECURSIVE_COLLISION
                    // (2) we did not reach the maximum number of loops
                    && --remainingLoops > 0
                    // (3) there have been changes in the last loop
                    && collisions[0] > 0
                    );
        }

        gameChangeGuard.lock();
        dynamicEntities.forEach(obj -> obj.update(currentTime, deltaTime));
        gameChangeGuard.unlock();
    }

    protected DirVector entityNetforce(GameEntity entity) {
        return DirVector.zeroVector();
    }

    /** calls {@link MovingEntity#applyCollision(float, float)} on each object of p for which it is valid */
    private void applyCollisions(Pair<Touchable, MovingEntity> p, float deltaTime, float previousTime) {
        p.right.applyCollision(deltaTime, previousTime);
        if (p.left instanceof MovingEntity) {
            ((MovingEntity) p.left).applyCollision(deltaTime, previousTime);
        }
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingEntity#applyCollision(float, float)}
     * @param p a pair of objects that may have collided.
     * @return true if this pair collided:
     * if this method returns false, then these objects do not collide and are not changed as a result.
     * if this method returns true, then these objects do collide and have this collision stored.
     * The collision can be calculated by {@link MovingEntity#applyCollision(float, float)} and applied by {@link Updatable#update(float)}
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
        if (allEntityPairs == null) {
            allEntityPairs = new ArrayList<>();

            // Naive solution: return all n^2 options
            // check all moving objects against (1: all other moving objects, 2: all static objects)
            dynamicEntities.forEach(obj -> {
                dynamicEntities.stream()
                        // only other objects
                        .filter(other -> other != obj)
                        .map(other -> new Pair<Touchable, MovingEntity>(other, obj))
                        .filter(pair -> !allEntityPairs.contains(pair))
                        .forEach(allEntityPairs::add);
                staticEntities
                        .forEach(other -> allEntityPairs.add(new Pair<>(other, obj)));
            });
        }

//        final long nulls = result.stream().filter(Objects::isNull).count();
//        if (nulls > 0) Toolbox.print("nulls: "+ nulls);

        collisionMax.updateAndPrint("Intersections", allEntityPairs.size(), "pairs");
        return allEntityPairs;
    }

    public void setLights(GL2 gl) {
        lights.forEach((pointLight) -> gl.setLight(pointLight.left, pointLight.right));
    }

    /**
     * draw all objects of the game
     */
    public void drawObjects(GL2 gl) {
//        Toolbox.drawAxisFrame(gl);

        // static objects cannot have interference
        staticEntities.forEach(d -> d.draw(gl));

        gameChangeGuard.lock();
        dynamicEntities.forEach(d -> d.draw(gl));
        gameChangeGuard.unlock();
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

    public void updateRenderTime() {
        time.updateRenderTime();
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

        /** game-seconds since creating this gametimer */
        private float currentInGameTime;
        /** last record of system time */
        private long lastMark;
        /** multiplication factor to multiply system time units to game-seconds */
        private static final float MUL_TO_SECONDS = 1E-9f;

        private final TrackedFloat gameTime;
        private final TrackedFloat renderTime;

        private GameTimer() {
            currentInGameTime = 0f;
            gameTime = new TrackedFloat(0f);
            renderTime = new TrackedFloat(-Settings.RENDER_DELAY);
            lastMark = System.nanoTime();
        }

        private void updateGameTime(){
            updateTimer();
            gameTime.update(currentInGameTime);
        }

        private void updateRenderTime(){
            updateTimer();
            renderTime.update(currentInGameTime - Settings.RENDER_DELAY);
        }

        public TrackedFloat getGameTime(){
            return gameTime;
        }

        public TrackedFloat getRenderTime(){
            return renderTime;
        }

        /** returns the current in-game time of this moment */
        public float time(){
            updateTimer();
            return currentInGameTime;
        }

        /** may be called anytime */
        private void updateTimer(){
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastMark) * MUL_TO_SECONDS;
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
