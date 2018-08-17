package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.AveragingQueue;
import nl.NG.Jetfightergame.Tools.DataStructures.ConcurrentArrayList;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static nl.NG.Jetfightergame.Settings.ServerSettings.DEBUG;
import static nl.NG.Jetfightergame.Settings.ServerSettings.MAX_COLLISION_ITERATIONS;

/**
 * @author Geert van Ieperen created on 10-3-2018.
 */
public class CollisionDetection implements EntityManagement {
    private CollisionEntity[] xLowerSorted;
    private CollisionEntity[] yLowerSorted;
    private CollisionEntity[] zLowerSorted;

    private AveragingQueue avgCollision = new AveragingQueue(ServerSettings.TARGET_TPS);
    private final Supplier<String> collisionCounter = () ->
            String.format("Collision pair count average: %1.01f", avgCollision.average());

    private final Collection<Touchable> staticEntities;
    private Collection<MovingEntity> dynamicEntities;
    private Collection<MovingEntity> newEntities;
    private Collection<MovingEntity> removeEntities;

    /**
     * Collects the given entities and allows collision and phisics calculations to influence these entities
     * @param staticEntities  a list of fixed entities. Entities in this collection should not move, but if they do,
     *                        dynamic objects might phase through when moving in opposite direction. Apart from this
     *                        case, the collision detection still functions.
     */
    public CollisionDetection(Collection<Touchable> staticEntities) {
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
        this.dynamicEntities = new CopyOnWriteArrayList<>();
        this.newEntities = new ConcurrentArrayList<>();
        this.removeEntities = new ConcurrentArrayList<>();

        Logger.printOnline(collisionCounter);

        int nOfEntities = staticEntities.size();
        xLowerSorted = new CollisionEntity[nOfEntities];
        yLowerSorted = new CollisionEntity[nOfEntities];
        zLowerSorted = new CollisionEntity[nOfEntities];

        int i = 0;
        for (Touchable entity : staticEntities) {
            CollisionEntity asCollisionEntity = new CollisionEntity(entity);
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
            i++;
        }

        Arrays.sort(xLowerSorted, (a, b) -> Float.compare(a.xLower(), b.xLower()));
        Arrays.sort(yLowerSorted, (a, b) -> Float.compare(a.yLower(), b.yLower()));
        Arrays.sort(zLowerSorted, (a, b) -> Float.compare(a.zLower(), b.zLower()));

    }

    @Override
    public void preUpdateEntities(NetForceProvider gravity, float deltaTime) {
        // add new entities
        if (!newEntities.isEmpty()) {
            mergeNewEntities(newEntities);
            newEntities.clear();
        }

        if (!removeEntities.isEmpty()) {
            deleteEntities(removeEntities);
            removeEntities.clear();
        }

        for (MovingEntity entity : dynamicEntities) {
            DirVector netForce = gravity.entityNetforce(entity);
            entity.preUpdate(deltaTime, netForce);
        }

        for (CollisionEntity entity : entityArray()) {
            entity.update();
        }

        Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
        Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);
        Toolbox.insertionSort(zLowerSorted, CollisionEntity::zLower);
    }

    @Override
    public void analyseCollisions(float currentTime, float deltaTime, PathDescription path) {
//        if (DEBUG) testInvariants();

        int remainingLoops = MAX_COLLISION_ITERATIONS;
        int nOfCollisions;

        do {
            nOfCollisions = 0;
            /* as a single collision may result in a previously not-intersecting pair to collide,
             * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
             * On the other hand, we may assume collisions of that magnitude appear seldom
             */
            PairList<Touchable, MovingEntity> pairs = getIntersectingPairs();

            Collision[] buffer = new Collision[pairs.size()];
            IntStream.range(0, pairs.size()).parallel()
                    .forEach(n -> buffer[n] = checkCollisionPair(pairs.left(n), pairs.right(n), deltaTime));

            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] == null) {
                    continue;
                }

                nOfCollisions++;

                Touchable other = pairs.left(i);
                if (other instanceof MovingEntity) { // if two entities collide
                    MovingEntity left = (MovingEntity) other;
                    MovingEntity.entityCollision(left, pairs.right(i), deltaTime);

                } else { // if entity collides with terrain
                    terrainCollision(pairs.right(i), path, deltaTime, buffer[i]);
                }
            }

            if (nOfCollisions > 0) {
                Logger.DEBUG.print(nOfCollisions + " collisions");
            }

        } while ((nOfCollisions > 0) && (--remainingLoops > 0) && !Thread.interrupted());

        if (nOfCollisions > 0) {
            Logger.WARN.print(nOfCollisions + " collisions not resolved");
        }
    }

    /**
     * gives a correcting momentum to the target entity, such that it restores its path. It may not happen that the
     * momentum results in another collision
     * @param deltaTime
     * @param collision the collision of this entity to be processed
     * @param target    an entity that has collided with a solid entity
     */
    private static void terrainCollision(MovingEntity target, PathDescription path, float deltaTime, Collision collision) {
        if (false && target instanceof AbstractJet) { // TODO pathDescription
            PosVector jetPosition = target.getPosition();
            PosVector bounceDirection = path.getMiddleOfPath(collision);
            DirVector targetToMid = jetPosition.to(bounceDirection, new DirVector());

            targetToMid.normalize();
            DirVector midToTarget = targetToMid.negate(new DirVector());

            float targetEnergy = target.getKineticEnergy(midToTarget) + ServerSettings.BASE_BUMPOFF_ENERGY;

            target.applyJerk(targetToMid, targetEnergy, deltaTime);

        } else {
            target.terrainCollision(deltaTime, collision);
        }
    }

    /**
     * @return null iff neither hits the other, otherwise return the resulting collision
     */
    private Collision checkCollisionPair(Touchable either, MovingEntity moving, float deltaTime) {
        // the isDead checks are for entities that die in a previous collision iteration
        if (TemporalEntity.isOverdue(moving)) return null;
        if (TemporalEntity.isOverdue(either)) return null;
        Collision collision = moving.checkCollisionWith(either, deltaTime);
        if (collision != null) return collision;

        if (either instanceof MovingEntity) {
            MovingEntity other = (MovingEntity) either;
            return other.checkCollisionWith(moving, deltaTime);
        }
        return null;
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private PairList<Touchable, MovingEntity> getIntersectingPairs() {
        CollisionEntity[] entityArray = entityArray();
        int nOfEntities = entityArray.length;

        // initialize id values to correspond to the array
        for (int i = 0; i < nOfEntities; i++) {
            entityArray[i].setId(i);
        }

        // this matrix is indexed using the entity id values, with i > j
        // if (adjacencyMatrix[i][j] == n) then entityArray[i] and entityArray[j] have n coordinates with coinciding intervals
        int[][] adjacencyMatrix = new int[nOfEntities][nOfEntities];

        checkOverlap(adjacencyMatrix, xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper);
        checkOverlap(adjacencyMatrix, yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper);
        checkOverlap(adjacencyMatrix, zLowerSorted, CollisionEntity::zLower, CollisionEntity::zUpper);

        PairList<Touchable, MovingEntity> allEntityPairs = new PairList<>(nOfEntities);

        // select all source pairs that are 'close' in three coordinates
        for (int i = 0; i < nOfEntities; i++) {
            Touchable entity = entityArray[i].entity;
            // skip world-on-world
            if (!(entity instanceof MovingEntity)) continue;

            for (int j = 0; j < i; j++) {
                // count in how many axes i overlaps j.
                int intervalOverlap = adjacencyMatrix[i][j];

                if (intervalOverlap >= 3) {
                    allEntityPairs.add(entityArray[j].entity, (MovingEntity) entity);
                }
            }
        }

        if (DEBUG) {
            boolean anyMatch = allEntityPairs.stream().anyMatch(p -> p.left.equals(p.right));
            if (anyMatch) Logger.DEBUG.print("duplicates found in intersecting pairs");
        }

        avgCollision.add(allEntityPairs.size());
        return allEntityPairs;
    }

    /**
     * tests whether the invariants holds.
     * Throws an error if any of the arrays is not correctly sorted or any other assumption no longer holds
     */
    private void testInvariants() {
        String source = Logger.getCallingMethod(1);
        Logger.DEBUG.printSpamless(source, "\n    " + source + " Checking collision detection invariants");

        // all arrays are of equal length
        if ((xLowerSorted.length != yLowerSorted.length) || (xLowerSorted.length != zLowerSorted.length)) {
            Logger.ERROR.print(toString(entityArray()));
            throw new IllegalStateException("Entity arrays have different lengths: "
                    + xLowerSorted.length + ", " + yLowerSorted.length + ", " + zLowerSorted.length
            );
        }

        // all arrays contain all entities
        Set<Touchable> allEntities = new HashSet<>();
        for (CollisionEntity collEty : entityArray()) {
            allEntities.add(collEty.entity);
        }
        for (CollisionEntity collEty : xLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array x does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : yLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array y does not contain entity " + collEty.entity);
            }
        }
        for (CollisionEntity collEty : zLowerSorted) {
            if (!allEntities.contains(collEty.entity)) {
                throw new IllegalStateException("Array z does not contain entity " + collEty.entity);
            }
        }

        // x is sorted
        float init = -Float.MAX_VALUE;
        for (int i = 0; i < xLowerSorted.length; i++) {
            CollisionEntity collisionEntity = xLowerSorted[i];
            if (collisionEntity.xLower() < init) {
                Logger.ERROR.print("Sorting error on x = " + i);
                Logger.ERROR.print(toString(xLowerSorted));
                throw new IllegalStateException("Sorting error on x = " + i);
            }
            init = collisionEntity.xLower();
        }

        // y is sorted
        init = -Float.MAX_VALUE;
        for (int i = 0; i < yLowerSorted.length; i++) {
            CollisionEntity collisionEntity = yLowerSorted[i];
            if (collisionEntity.yLower() < init) {
                Logger.ERROR.print("Sorting error on y = " + i);
                Logger.ERROR.print(toString(yLowerSorted));
                throw new IllegalStateException("Sorting error on y = " + i);
            }
            init = collisionEntity.yLower();
        }

        // z is sorted
        init = -Float.MAX_VALUE;
        for (int i = 0; i < zLowerSorted.length; i++) {
            CollisionEntity collisionEntity = zLowerSorted[i];
            if (collisionEntity.zLower() < init) {
                Logger.ERROR.print("Sorting error on z = " + i);
                Logger.ERROR.print(toString(zLowerSorted));
                throw new IllegalStateException("Sorting error on z = " + i);
            }
            init = collisionEntity.zLower();
        }
    }

    private static String toString(CollisionEntity[] entityArray) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < entityArray.length; i++) {
            CollisionEntity ety = entityArray[i];
            s.append(String.format("%3d | %3d : %s\n", i, ety.id, ety.entity));
        }
        return s.toString();
    }

    /**
     * iterating over the sorted array, increase the value of all pairs that have coinciding intervals
     * @param adjacencyMatrix the matrix where the pairs are marked using entity id's
     * @param sortedArray     an array sorted increasingly on the lower mapping
     * @param lower           a mapping that maps to the lower value of the interval of the entity
     * @param upper           a mapping that maps an entity to its upper interval
     */
    protected void checkOverlap(int[][] adjacencyMatrix, CollisionEntity[] sortedArray, Function<CollisionEntity, Float> lower, Function<CollisionEntity, Float> upper) {
        // INVARIANT:
        // all items i where i.lower < source.lower, are already added to the matrix

        int nOfItems = sortedArray.length;
        for (int i = 0; i < (nOfItems - 1); i++) {
            CollisionEntity subject = sortedArray[i];

            // increases the checks count of every source with index less than i, with position less than the given minimum
            int j = i + 1;
            CollisionEntity target = sortedArray[j++];

            // while the lowerbound of target is less than the upperbound of our subject
            while (lower.apply(target) <= upper.apply(subject)) {

                adjacencyMatrix[subject.id][target.id]++;
                adjacencyMatrix[target.id][subject.id]++;

                if (j == nOfItems) break;
                target = sortedArray[j++];
            }
        }
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> entities) {
        newEntities.addAll(entities);
    }

    @Override
    public void addEntity(MovingEntity entity) {
        newEntities.add(entity);
    }

    @Override
    public void removeEntity(MovingEntity entity) {
        removeEntities.add(entity);
    }

    private void mergeNewEntities(Collection<MovingEntity> newEntities) {
        int nOfNewEntities = newEntities.size();
        if (nOfNewEntities <= 0) return;

        CollisionEntity[] newXSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newYSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newZSort = new CollisionEntity[nOfNewEntities];

        int i = 0;
        for (MovingEntity newEntity : newEntities) {
            CollisionEntity asCollisionEntity = new CollisionEntity(newEntity);
            newXSort[i] = asCollisionEntity;
            newYSort[i] = asCollisionEntity;
            newZSort[i] = asCollisionEntity;
            i++;
        }

        Toolbox.insertionSort(newXSort, CollisionEntity::xLower);
        Toolbox.insertionSort(newYSort, CollisionEntity::yLower);
        Toolbox.insertionSort(newZSort, CollisionEntity::zLower);

        xLowerSorted = Toolbox.mergeArrays(xLowerSorted, newXSort, CollisionEntity::xLower);
        yLowerSorted = Toolbox.mergeArrays(yLowerSorted, newYSort, CollisionEntity::yLower);
        zLowerSorted = Toolbox.mergeArrays(zLowerSorted, newZSort, CollisionEntity::zLower);

        dynamicEntities.addAll(newEntities);
    }

    /**
     * Remove the selected entities off the entity lists in a robust way. Entities that did not exist are ignored, and
     * doubles are also accepted.
     * @param targets a collection of entities to be removed
     */
    private void deleteEntities(Collection<MovingEntity> targets) {
        xLowerSorted = deleteAll(targets, xLowerSorted);
        yLowerSorted = deleteAll(targets, yLowerSorted);
        zLowerSorted = deleteAll(targets, zLowerSorted);

        dynamicEntities.removeAll(targets);
    }

    private CollisionEntity[] deleteAll(Collection<MovingEntity> targets, CollisionEntity[] array) {
        int xi = 0;
        for (int i = 0; i < array.length; i++) {
            Touchable entity = array[i].entity;
            if ((entity instanceof MovingEntity) && targets.contains(entity)) {
                continue;
            }
            array[xi++] = array[i];
        }
        return Arrays.copyOf(array, xi);
    }

    /**
     * casts a ray into the world and returns where it hits anything.
     * @param source    a point in world-space
     * @param direction a direction in world-space
     * @return the collision caused by this ray
     */
    public Collision rayTrace(PosVector source, DirVector direction) {
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
                newCrash.convertToGlobal(sm, null);
                firstHit.check(newCrash);
            }
        };

        for (CollisionEntity e : entityArray()) {
            Touchable entity = e.entity;
            entity.toLocalSpace(sm, () -> entity.create(sm, tracer));
        }

        return firstHit.get();
    }

    /**
     * @return an array of the entities, backed by any local representation. Should only be used for querying, otherwise it must be cloned
     */
    private CollisionEntity[] entityArray() {
        return xLowerSorted;
    }

    @Override
    public Collection<Touchable> getStaticEntities() {
        // is unmodifiable
        return Collections.unmodifiableCollection(staticEntities);
    }

    @Override
    public Collection<MovingEntity> getDynamicEntities() {
        Collection<MovingEntity> l = new ArrayList<>(dynamicEntities);
        l.addAll(newEntities);
        return l;
    }

    @Override
    public void updateEntities(float currentTime) {
        for (MovingEntity entity : dynamicEntities) {
            entity.update(currentTime);
        }
    }

    @Override
    public void cleanUp() {
        Logger.removeOnlineUpdate(collisionCounter);
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];
    }

    protected class CollisionEntity {
        public final Touchable entity;
        public int id;

        public float range;
        private float x;
        private float y;
        private float z;

        public CollisionEntity(Touchable source) {
            this.entity = source;
            update();
        }

        public void update() {
            PosVector middle = entity.getExpectedMiddle();
            this.range = entity.getRange();
            x = middle.x;
            y = middle.y;
            z = middle.z;
        }

        public void setId(int id) {
            this.id = id;
        }

        public float xUpper() {
            return x + range;
        }

        public float yUpper() {
            return y + range;
        }

        public float zUpper() {
            return z + range;
        }

        public float xLower() {
            return x - range;
        }

        public float yLower() {
            return y - range;
        }

        public float zLower() {
            return z - range;
        }

        @Override
        public String toString() {
            return entity.toString();
        }
    }
}
