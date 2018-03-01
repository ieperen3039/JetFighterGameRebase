package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.EnemyFlyingTarget;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.*;
import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.NG.Jetfightergame.Settings.DEBUG;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public abstract class GameState implements Environment {

    private static final int COLLISION_COUNT_AVERAGE = 5;
    private AveragingQueue avgCollision = new AveragingQueue(COLLISION_COUNT_AVERAGE);
    private final Consumer<ScreenOverlay.Painter> collisionCounter = (hud) ->
            hud.printRoll(String.format("Collision count: %1.01f", avgCollision.average()));

    protected Collection<Touchable> staticEntities = new ConcurrentArrayList<>();
    protected Collection<MovingEntity> dynamicEntities = new ConcurrentArrayList<>();
    protected Collection<Particle> particles = new ConcurrentArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ConcurrentArrayList<>();
    private Collection<MovingEntity> newEntities = new ConcurrentArrayList<>();

    protected final Player player;

    private Collection<Pair<Touchable, MovingEntity>> allEntityPairs = null;

    private final GameTimer time;

    public GameState(Player player, GameTimer time) {
        this.player = player;
        this.time = time;
        ScreenOverlay.addHudItem(collisionCounter);
    }

    private Extreme<Integer> collisionMax = new Extreme<>(true);

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        final float currentTime = time.getGameTime().current();
        final float deltaTime = time.getGameTime().difference();

        if (deltaTime == 0) return;

        // update positions and apply physics
        dynamicEntities.parallelStream()
                .forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));
        dynamicEntities.addAll(newEntities);
        newEntities.clear();

        if ((Settings.MAX_COLLISION_ITERATIONS != 0) && (deltaTime > 0))
            analyseCollisions(currentTime, deltaTime);

        dynamicEntities.forEach(e -> e.update(currentTime));
        dynamicEntities.removeIf(entity -> (entity instanceof MortalEntity) && ((MortalEntity) entity).isDead());
    }

    /**
     * checks and resolves all collisions that occurred in the given timeperiod
     * @param currentTime the current game-loop time
     * @param deltaTime the in-game time difference from the last call to this method
     */
    private void analyseCollisions(float currentTime, float deltaTime) {
        int remainingLoops = Settings.MAX_COLLISION_ITERATIONS;
        int newCollisions = 0;

        List<Pair<Touchable, MovingEntity>> collisionPairs;

        do {
            /* as a single collision may result in a previously not-intersecting pair to collide,
             * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
             * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
             */
            collisionPairs = getIntersectingPairs()
                    .parallelStream()
                    // check for collisions
                    .filter(closeTarget -> checkCollisionPair(closeTarget.right, closeTarget.left, deltaTime))
                    .collect(Collectors.toList());

            newCollisions += collisionPairs.size();

            // process the final collisions in pairs
            List<RigidBody> postCollisions = collisionPairs.stream()
                    .flatMap(p -> {
                        RigidBody left = p.left.getFinalCollision(deltaTime);
                        RigidBody right = p.right.getFinalCollision(deltaTime);
                        RigidBody.process(left, right);
                        return Stream.of(left, right);
                    })
                    .distinct()
                    .collect(Collectors.toList());

            // apply the collisions to the objects
            postCollisions.parallelStream()
                    .forEach(r -> r.apply(deltaTime, currentTime));
            postCollisions.clear();

        } while (!collisionPairs.isEmpty() && (--remainingLoops > 0) && !Thread.interrupted());

        avgCollision.add(newCollisions);

        if (!collisionPairs.isEmpty()) {
            Toolbox.print(collisionPairs.size() + " collisions not resolved after " + newCollisions + " calculations");
        }

    }

    /**
     * @return false iff neither hits the other
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean checkCollisionPair(MovingEntity moving, Touchable either, float deltaTime) {
        if ((moving instanceof MortalEntity) && ((MortalEntity) moving).isDead()) return false;
        if ((either instanceof MortalEntity) && ((MortalEntity) either).isDead()) return false;
        if (moving.checkCollisionWith(either, deltaTime)) return true;
        return (either instanceof MovingEntity) && ((MovingEntity) either).checkCollisionWith(moving, deltaTime);
    }

    protected abstract DirVector entityNetforce(MovingEntity entity);

    /**
     * TODO efficient implementation, Possibly move to dedicated class
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        allEntityPairs = new ConcurrentArrayList<>();

        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        for (MovingEntity obj : dynamicEntities) {
            dynamicEntities.stream()
                    // this makes sure that one object pair does not occur the other way around.
                    // It compares in a random, yet consistent way
                    .filter(other -> other.hashCode() >= obj.hashCode())
                    // as hashcode does not guarantees an identifier, we can not assume equality.
                    .filter(other -> !other.equals(obj))
                    .map(other -> new Pair<Touchable, MovingEntity>(other, obj))
                    .distinct()
                    .forEach(allEntityPairs::add);
            staticEntities.stream()
                    .map(other -> new Pair<>(other, obj))
                    .forEach(pair -> allEntityPairs.add(pair));
        }

        if (DEBUG) {
            final long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
            if (nulls > 0) Toolbox.print("nulls found by intersecting pairs: " + nulls);
        }

        collisionMax.updateAndPrint("Intersections", allEntityPairs.size(), "pairs");
        return allEntityPairs;
    }

    @Override
    public void setLights(GL2 gl) {
        for (Pair<PosVector, Color4f> l : lights) {
            final PosVector pos = l.left;
            final Color4f color = l.right;

            gl.setLight(pos, color);

            if (Settings.SHOW_LIGHT_POSITIONS) {
                gl.setMaterial(Material.GLOWING, color);
                gl.pushMatrix();
                {
                    gl.translate(pos);
                    gl.scale(0.1f);
                    gl.draw(GeneralShapes.INVERSE_CUBE);
                }
                gl.popMatrix();
            }
        }
    }

    @Override
    public void drawObjects(GL2 gl) {
//        Toolbox.drawAxisFrame(gl);
        staticEntities.forEach(d -> d.draw(gl));

        glDisable(GL_CULL_FACE); // TODO when new meshes are created or fixed, this should be removed
        dynamicEntities.forEach(d -> d.draw(gl));
    }

    @Override
    public void drawParticles(GL2 gl) {
        final Float deltaTime = time.getRenderTime().difference();

        particles.forEach(p -> p.updateRender(deltaTime));
        particles.removeIf(Particle::isOverdue);
        particles.forEach(p -> p.draw(gl));
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    @Override
    public void addEntity(MovingEntity entity) {
        newEntities.add(entity);
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> entities) {
        newEntities.addAll(entities);
    }

    @Override
    public void addParticles(Collection<Particle> newParticles) {
        particles.addAll(newParticles);
    }

    @Override
    public HUDTargetable getHUDTarget(MovingEntity entity) {
        return new EnemyFlyingTarget(entity, () -> player.jet().interpolatedPosition());
    }

    /**
     * (this method may be reduced to accessing the lock)
     */
    public void cleanUp() {
        dynamicEntities.clear();
        staticEntities.clear();
        lights.clear();
        particles.clear();
        ScreenOverlay.removeHudItem(collisionCounter);
        if (allEntityPairs != null) allEntityPairs.clear();
        System.gc();
    }
}
