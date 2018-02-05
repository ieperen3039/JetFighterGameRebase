package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.Scenarios.Environment;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Tools.AveragingQueue;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

    protected Collection<Touchable> staticEntities = new ArrayList<>();
    protected Collection<MovingEntity> dynamicEntities = new ArrayList<>();
    protected Collection<Particle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    private Collection<Pair<Touchable, MovingEntity>> allEntityPairs = null;

    private final GameTimer time;

    private ReentrantReadWriteLock entityModificationLock = new ReentrantReadWriteLock();
    /** used when a list is iterated, but all items stay in this list */
    private Lock readLock = entityModificationLock.readLock();
    /** used when items in a list are removed, added or any combination of these */
    private Lock writeLock = entityModificationLock.writeLock();

    public GameState(GameTimer time) {
        this.time = time;
        ScreenOverlay.addHudItem(collisionCounter);
    }

    private Extreme<Integer> collisionMax = new Extreme<>(true);

    public abstract void buildScene(Player player);

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        final float currentTime = time.getGameTime().current();
        final float deltaTime = time.getGameTime().difference();

        if (deltaTime == 0) return;

        // update positions and apply physics
        readLock.lock();
        dynamicEntities.parallelStream()
                .forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));
        readLock.unlock();

        int remainingLoops = Settings.MAX_COLLISION_ITERATIONS;
        // check and handle collisions
        if ((deltaTime > 0f) && (remainingLoops != 0)) {

            int newCollisions = 0;
            List<Pair<Touchable, MovingEntity>> collisionPairs;
            List<RigidBody> postCollisions = new ArrayList<>();

            do {
                /* as a single collision may result in a previously not-intersecting pair to collide,
                 * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
                 * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
                 */
                final Collection<Pair<Touchable, MovingEntity>> closeTargets = getIntersectingPairs();
                collisionPairs = closeTargets.stream()
                        // check for collisions
                        .filter(closeTarget -> checkCollisionPair(closeTarget.right, closeTarget.left, deltaTime))
                        .collect(Collectors.toList());

                newCollisions += collisionPairs.size();
                postCollisions.clear();
                // caches previously calculated rigid body calculations
                Map<Touchable, RigidBody> finalCollisions = new HashMap<>();

                // process the final collisions in pairs
                postCollisions = collisionPairs.stream()
                        .flatMap(p -> {
                            RigidBody left = p.left.getRigidBody(finalCollisions, deltaTime);
                            RigidBody right = p.right.getRigidBody(finalCollisions, deltaTime);
                            RigidBody.process(left, right);
                            return Stream.of(left, right);
                        })
                        .distinct()
                        .collect(Collectors.toList());

                // apply the collisions to the objects
                postCollisions
                        .forEach(r -> r.apply(deltaTime, currentTime));

            } while (!collisionPairs.isEmpty() && (--remainingLoops > 0) && !Thread.interrupted());

            avgCollision.add(newCollisions);

            if (remainingLoops == 0) {
                Toolbox.print(collisionPairs.size() + " collisions not resolved after " + newCollisions + " calculations");
            }
        }

        writeLock.lock();
        Iterator<MovingEntity> iterator = dynamicEntities.iterator();
        while (iterator.hasNext()) {
            MovingEntity entity = iterator.next();
            entity.update(currentTime);
            if (entity instanceof MortalEntity) {
                MortalEntity unit = (MortalEntity) entity;
                if (unit.isDead()) {
                    particles.addAll(unit.explode());
                    iterator.remove();
                }
            }
        }
        writeLock.unlock();

    }

    /**
     * @return false iff neither hits the other
     */
    public boolean checkCollisionPair(MovingEntity moving, Touchable either, float deltaTime) {
        return moving.checkCollisionWith(either, deltaTime) || (
                        (either instanceof MovingEntity) && ((MovingEntity) either).checkCollisionWith(moving, deltaTime)
        );
    }

    protected abstract DirVector entityNetforce(MovingEntity entity);

    /** TODO efficient implementation, Possibly move to dedicated class
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        allEntityPairs = new ArrayList<>();

        readLock.lock();
        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        dynamicEntities.forEach(obj -> {
            dynamicEntities.stream()
                    // this makes sure that one object pair does not occur the other way around.
                    .filter(other -> other.hashCode() >= obj.hashCode())
                    // as hashcode does not guarantees an identifier, we can not assume equality.
                    .filter(other -> !other.equals(obj))
                    .map(other -> new Pair<Touchable, MovingEntity>(other, obj))
                    .distinct()
                    .forEach(allEntityPairs::add);
            staticEntities
                    .forEach(other -> allEntityPairs.add(new Pair<>(other, obj)));
        });
        readLock.unlock();

        if (DEBUG) {
            final long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
            if (nulls > 0) Toolbox.print("nulls found by intersecting pairs: " + nulls);
        }

        collisionMax.updateAndPrint("Intersections", allEntityPairs.size(), "pairs");
        return allEntityPairs;
    }

    @Override
    public void setLights(GL2 gl) {
        readLock.lock();

        for (Pair<PosVector, Color4f> l : lights) {
            final PosVector pos = l.left;
            final Color4f color = l.right;

            gl.setLight(pos, color);

            if (Settings.SHOW_LIGHT_POSITIONS){
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

        readLock.unlock();
    }

    @Override
    public void drawObjects(GL2 gl) {
//        Toolbox.drawAxisFrame(gl);
        readLock.lock();

        staticEntities.forEach(d -> d.draw(gl));

        glDisable(GL_CULL_FACE); // TODO when new meshes are created or fixed, this should be removed
        dynamicEntities.forEach(d -> d.draw(gl));

        readLock.unlock();
    }

    @Override
    public void drawParticles(GL2 gl){
        readLock.lock();
        particles.forEach(p -> p.updateRender(time.getRenderTime().difference()));
        readLock.unlock();

        writeLock.lock();
        particles.removeIf(Particle::isOverdue);
        writeLock.unlock();

        readLock.lock();
        particles.forEach(p -> p.draw(gl));
        readLock.unlock();
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    @Override
    public int getNumberOfLights() {
        return lights.size();
    }

    /**
     * (this method may be redundant)
     */
    public void cleanUp() {
        try {
            writeLock.lockInterruptibly();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            ScreenOverlay.removeHudItem(collisionCounter);
            dynamicEntities.clear();
            staticEntities.clear();
            lights.clear();
            particles.clear();
            if (allEntityPairs != null) allEntityPairs.clear();
            System.gc();
        }
    }
}
