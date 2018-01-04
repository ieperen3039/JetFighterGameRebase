package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.NG.Jetfightergame.Engine.Settings.DEBUG;
import static nl.NG.Jetfightergame.Engine.Settings.RENDER_DELAY;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class GameState {

    private AbstractJet playerJet;

    protected Collection<Touchable> staticEntities = new ArrayList<>();
    protected Collection<MovingEntity> dynamicEntities = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

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
        float currentTime = time.getGameTime().current();
        float deltaTime = time.getGameTime().difference();


        // update positions and apply physics
        dynamicEntities.parallelStream()
                .forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));

        int remainingLoops = Settings.MAX_COLLISION_ITERATIONS;
        // check and handle collisions
        if (deltaTime > 0f && remainingLoops != 0) {

            int totalCollisions = 0;
            Collection<Pair<Touchable, MovingEntity>> closeTargets = getIntersectingPairs();
            Collection<MovingEntity> collisions;
            do {
                /* as a single collision may result in a previously not-intersecting pair to collide,
                 * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
                 * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
                 */
                collisions = closeTargets.parallelStream()
                        // check for collisions
                        .filter(GameState::checkPair)
                        // extract and collect all distinct moving collided elements to update them
                        .flatMap(p -> Stream.of(p.left, p.right))
                        .filter(e -> e instanceof MovingEntity)
                        .map(e -> (MovingEntity) e)
                        .distinct()
                        .collect(Collectors.toList());

                totalCollisions += collisions.size();

                collisions.parallelStream()
                        .forEach(p -> p.applyCollision(currentTime));

            } while (collisions.size() > 0 && --remainingLoops > 0);
            if (remainingLoops == 0) {
                Toolbox.print(collisions.size() + " collision not resolved after " + totalCollisions + " collisions");
            } else if (totalCollisions > 0 && DEBUG) {
                Toolbox.print("processed " + totalCollisions + " collisions");
            }
        }

        dynamicEntities.forEach(obj -> obj.update(currentTime));
    }

    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingEntity#applyCollision(float)}
     * @param p a pair of objects that may have collided.
     * @return true if this pair collided:
     * if this method returns false, then these objects do not collide and are not changed as a result.
     * if this method returns true, then these objects do collide and have this collision stored.
     * The collision can be calculated by {@link MovingEntity#applyCollision(float)} and applied by {@link Updatable#update(float)}
     */
    private static boolean checkPair(Pair<Touchable, MovingEntity> p) {
        MovingEntity moving = p.right;
        Touchable either = p.left;

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
                        .filter(other -> other != obj)
                        .map(other -> new Pair<Touchable, MovingEntity>(other, obj))
                        .distinct()
                        .forEach(allEntityPairs::add);
                staticEntities
                        .forEach(other -> allEntityPairs.add(new Pair<>(other, obj)));
            });
            if (DEBUG) {
                final long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
                if (nulls > 0) Toolbox.print("nulls found by intersecting pairs: " + nulls);
            }
        }


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
        Toolbox.drawAxisFrame(gl);

        staticEntities.forEach(d -> d.draw(gl));
        dynamicEntities.forEach(d -> d.draw(gl));
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
            renderTime = new TrackedFloat(-RENDER_DELAY);
            lastMark = System.nanoTime();
        }

        private void updateGameTime(){
            updateTimer();
            gameTime.update(currentInGameTime);
        }

        private void updateRenderTime(){
            updateTimer();
            renderTime.update(currentInGameTime - RENDER_DELAY);
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
