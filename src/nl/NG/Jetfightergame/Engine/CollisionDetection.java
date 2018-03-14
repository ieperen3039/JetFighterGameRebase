package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.NG.Jetfightergame.Settings.DEBUG;

/**
 * @author Geert van Ieperen
 * created on 10-3-2018.
 */
public class CollisionDetection {

    private Touchable[] xSort;
    private Touchable[] ySort;
    private Touchable[] zSort;

    private final Collection<Touchable> staticEntities;
    private final Collection<MovingEntity> dynamicEntities;

    private Extreme<Integer> collisionMax = new Extreme<>(true);
    private final GameTimer time;

    public CollisionDetection(GameTimer time, Collection<MovingEntity> dynamicEntities, Collection<Touchable> staticEntities) {
        this.time = time;
        this.dynamicEntities = dynamicEntities;
        this.staticEntities = staticEntities;
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

        //avgCollision.add(newCollisions);

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

    /**
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        Collection<Pair<Touchable, MovingEntity>> allEntityPairs = new ArrayList<>();

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
                    .forEach(allEntityPairs::add);
        }

        if (DEBUG) {
            final long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
            if (nulls > 0) Toolbox.print("nulls found by intersecting pairs: " + nulls);
        }

        collisionMax.updateAndPrint("Intersections", allEntityPairs.size(), "pairs");
        return allEntityPairs;
    }

    public void update(Collection<MovingEntity> newEntities) {
        analyseCollisions(time.getGameTime().current(), time.getGameTime().difference());
    }

    /**
     * casts a ray into the world and returns where it hits anything.
     * @param source a point in world-space
     * @param direction a direction in world-space
     * @return the collision caused by this ray
     */
    public Collision rayTrace(PosVector source, DirVector direction){
        ShadowMatrix sm = new ShadowMatrix();
        PosVector sink = source.add(direction, new PosVector());

        Extreme<Collision> firstHit = new Extreme<>(false);

        final Consumer<Shape> tracer = shape -> {
            PosVector alpha = sm.mapToLocal(source);
            PosVector beta = sm.mapToLocal(sink);
            final DirVector alphaToBeta = alpha.to(beta, new DirVector());

            // search hitpoint, add it when found
            Collision newCrash = shape.getCollision(alpha, alphaToBeta, null);
            if (newCrash != null) {
                newCrash.convertToGlobal(sm);
                firstHit.check(newCrash);
            }
        };

        dynamicEntities.parallelStream()
                .forEach(entity -> entity.toLocalSpace(sm, () -> entity.create(sm, tracer)));
        staticEntities.parallelStream()
                .forEach(entity -> entity.toLocalSpace(sm, () -> entity.create(sm, tracer)));

        return firstHit.get();
    }
}
