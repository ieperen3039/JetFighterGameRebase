package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.AveragingQueue;
import nl.NG.Jetfightergame.Tools.DataStructures.ConcurrentArrayList;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @param dynamicEntities a list of moving entities. This object is not backed by the given list, changes in the
     *                        list have no effect on this object.
     * @param staticEntities  a list of fixed entities. Entities in this collection should not move, but if they do,
     *                        dynamic objects might phase through when moving in opposite direction. Apart from this
     *                        case, the collision detection still functions.
     */
    public CollisionDetection(Collection<MovingEntity> dynamicEntities, Collection<Touchable> staticEntities) {
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
        this.newEntities = new ConcurrentArrayList<>();
        this.removeEntities = new ConcurrentArrayList<>();

        Logger.printOnline(collisionCounter);

        int nOfEntities = dynamicEntities.size() + staticEntities.size();
        xLowerSorted = new CollisionEntity[nOfEntities];
        yLowerSorted = new CollisionEntity[nOfEntities];
        zLowerSorted = new CollisionEntity[nOfEntities];

        Iterator<CollisionEntity> entities = Stream
                // combine the dynamic entities with the static entities
                .concat(dynamicEntities.stream(), staticEntities.stream())
                // create new collision entities of them
                .map(CollisionEntity::new)
                .iterator();

        for (int i = 0; entities.hasNext(); i++) {
            CollisionEntity asCollisionEntity = entities.next();
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
        }

        Arrays.sort(xLowerSorted, (a, b) -> Float.compare(a.xLower(), b.xLower()));
        Arrays.sort(yLowerSorted, (a, b) -> Float.compare(a.yLower(), b.yLower()));
        Arrays.sort(zLowerSorted, (a, b) -> Float.compare(a.zLower(), b.zLower()));

        this.dynamicEntities = new ArrayList<>(dynamicEntities);
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
        int remainingLoops = MAX_COLLISION_ITERATIONS;

        // the pairs that have their collision been processed
        List<Pair<Touchable, MovingEntity>> collisionPairs;

        do {
            /* as a single collision may result in a previously not-intersecting pair to collide,
             * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
             * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
             * On the other hand, we may assume collisions of that magnitude appear seldom
             */
            collisionPairs = getIntersectingPairs()
                    .parallelStream()
                    // check for collisions, remove items that did not collide
                    .filter(p -> checkCollisionPair(p.left, p.right, deltaTime))
                    .collect(Collectors.toList());


            for (Pair<Touchable, MovingEntity> pair : collisionPairs) {
                if (pair.left instanceof MovingEntity) { // if two entities collide
                    MovingEntity left = (MovingEntity) pair.left;
                    bumpOff(left, pair.right, deltaTime);

                } else { // if entity collides with terrain
                    applyCorrection(pair.right, path, deltaTime);
                }
            }


        } while (!collisionPairs.isEmpty() && (--remainingLoops > 0) && !Thread.interrupted());

        if (!collisionPairs.isEmpty()) {
            Logger.printError(collisionPairs.size() + " collisions not resolved");
        }
    }

    /**
     * move two entities away from each other
     * @param left      one entity, which has collided with right
     * @param right     another entity, which has collided with left
     * @param deltaTime time difference of this gameloop
     */
    private void bumpOff(MovingEntity left, MovingEntity right, float deltaTime) {
        DirVector leftToRight = new DirVector();
        leftToRight = left.getExpectedPosition().to(right.getExpectedPosition(), leftToRight);

        leftToRight.normalize();
        DirVector rightToLeft = leftToRight.negate(new DirVector());

        float rightEnergy = right.getKineticEnergy(rightToLeft);
        float leftEnergy = left.getKineticEnergy(leftToRight);
        float sharedEnergy = (0.3f * (leftEnergy + rightEnergy)) + ServerSettings.BASE_BUMPOFF_ENERGY; // not quite but ok

        right.applyJerk(leftToRight, sharedEnergy, deltaTime);
        left.applyJerk(rightToLeft, sharedEnergy, deltaTime);

    }

    /**
     * gives a correcting momentum to the target entity, such that it restores its path. It may not happen that the
     * momentum results in another collision
     * @param deltaTime
     * @param target    an entity that has collided with a solid entity
     */
    private void applyCorrection(MovingEntity target, PathDescription path, float deltaTime) {
//        if (target instanceof AbstractJet) {
        PosVector jetPosition = target.getPosition();
        PosVector bounceDirection = path.getMiddleOfPath(jetPosition);
        DirVector targetToMid = jetPosition.to(bounceDirection, new DirVector());

        targetToMid.normalize();
        DirVector midToTarget = targetToMid.negate(new DirVector());

        float targetEnergy = target.getKineticEnergy(midToTarget) + ServerSettings.BASE_BUMPOFF_ENERGY;

        target.applyJerk(targetToMid, targetEnergy, deltaTime);

//        } else {
//            target.elasticCollision();
//        }
    }

    /**
     * @return false iff neither hits the other
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean checkCollisionPair(Touchable either, MovingEntity moving, float deltaTime) {
        // the isDead checks are for entities that die in a previous collision iteration
        if (TemporalEntity.isOverdue(moving)) return false;
        if (TemporalEntity.isOverdue(either)) return false;
        if (moving.checkCollisionWith(either, deltaTime)) return true;
        return (either instanceof MovingEntity) && ((MovingEntity) either).checkCollisionWith(moving, deltaTime);
    }

    /**
     * generate a list (possibly empty) of all pairs of objects that may have collided. This can include (parts of) the
     * ground, but not an object with itself. One pair does not occur the other way around.
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        CollisionEntity[] entityArray = entityArray();
        int nOfEntities = entityArray.length;

        // initialize id values to correspond to the array
        for (int i = 0; i < nOfEntities; i++) {
            entityArray[i].setId(i);
        }

        // this matrix is indexed using the entity id values, with i > j
        // if (adjacencyMatrix[i][j] == 2) then entityArray[i] and entityArray[j] have 2 coordinates with coinciding intervals
        int[][] adjacencyMatrix = new int[nOfEntities][nOfEntities];

        checkOverlap(adjacencyMatrix, xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper);
        checkOverlap(adjacencyMatrix, yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper);
        checkOverlap(adjacencyMatrix, zLowerSorted, CollisionEntity::zLower, CollisionEntity::zUpper);

        Collection<Pair<Touchable, MovingEntity>> allEntityPairs = new ArrayList<>();

        // select all source pairs that are 'close' in three coordinates
        for (int i = 0; i < nOfEntities; i++) {
            Touchable entity = entityArray[i].entity;
            // skip world-on-world
            if (!(entity instanceof MovingEntity)) continue;

            for (int j = i - 1; j > 0; j--) {
                // count how often i hits j and how often j hits i.
                int intervalAgreements = adjacencyMatrix[i][j];

                if (intervalAgreements >= 3) {
                    Pair<Touchable, MovingEntity> newPair = new Pair<>(entityArray[j].entity, (MovingEntity) entity);
                    allEntityPairs.add(newPair);
                }
            }
        }

        // run some checks
        if (DEBUG) {
            long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
            if (nulls > 0) Logger.print("nulls found in intersecting pairs: " + nulls);
            long equals = allEntityPairs.stream().filter(p -> p.left.equals(p.right)).count();
            if (equals > 0) Logger.print("duplicates found in intersecting pairs: " + equals);
        }

        avgCollision.add(allEntityPairs.size());

        return allEntityPairs;
    }

    /**
     * tests whether the invariant holds. Throws an error if any of the arrays is not correctly sorted
     */
    public void testSort() {
        Logger.printSpamless("testSort", "\n" + Logger.getCallingMethod(1) + " Testing Sorting");
        float init = -Float.MAX_VALUE;
        for (int i = 0; i < xLowerSorted.length; i++) {
            CollisionEntity collisionEntity = xLowerSorted[i];
            if (collisionEntity.xLower() < init) {
                Logger.printError("Sorting error on x = " + i);
                Logger.printError(Arrays.toString(xLowerSorted));
                Toolbox.exitJava();
            }
            init = collisionEntity.xLower();
        }

        init = -Float.MAX_VALUE;
        for (int i = 0; i < yLowerSorted.length; i++) {
            CollisionEntity collisionEntity = yLowerSorted[i];
            if (collisionEntity.yLower() < init) {
                Logger.printError("Sorting error on y = " + i);
                Logger.printError(Arrays.toString(yLowerSorted));
            }
            init = collisionEntity.yLower();
        }

        init = -Float.MAX_VALUE;
        for (int i = 0; i < zLowerSorted.length; i++) {
            CollisionEntity collisionEntity = zLowerSorted[i];
            if (collisionEntity.zLower() < init) {
                Logger.printError("Sorting error on z = " + i);
                Logger.printError(Arrays.toString(zLowerSorted));
            }
            init = collisionEntity.zLower();
        }
    }

    /**
     * iterating over xLowerSorted, increase the value of all pairs that have coinciding intervals
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

                // add one to the number of coinciding coordinates
                if (subject.id > target.id) {
                    adjacencyMatrix[subject.id][target.id]++;
                } else {
                    adjacencyMatrix[target.id][subject.id]++;
                }

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
     * remove the selected entities off the entity lists in a robust way (entities that did not exist are accepted, as
     * well as doubles)
     * @param targets a collection of entities to be removed
     */
    private void deleteEntities(Collection<MovingEntity> targets) {
        int nOfEntities = xLowerSorted.length;

        int xi = 0;
        CollisionEntity[] xList = new CollisionEntity[nOfEntities];
        for (CollisionEntity target : xLowerSorted) {
            Touchable entity = target.entity;
            if ((entity instanceof MovingEntity) && targets.contains(entity)) {
                continue;
            }
            xList[xi++] = target;
        }

        int yi = 0;
        CollisionEntity[] yList = new CollisionEntity[nOfEntities];
        for (CollisionEntity target : yLowerSorted) {
            Touchable entity = target.entity;
            if ((entity instanceof MovingEntity) && targets.contains(entity)) {
                continue;
            }
            yList[yi++] = target;
        }

        int zi = 0;
        CollisionEntity[] zList = new CollisionEntity[nOfEntities];
        for (CollisionEntity target : zLowerSorted) {
            Touchable entity = target.entity;
            if ((entity instanceof MovingEntity) && targets.contains(entity)) {
                continue;
            }
            zList[zi++] = target;
        }

        xLowerSorted = Arrays.copyOf(xList, xi);
        yLowerSorted = Arrays.copyOf(yList, yi);
        zLowerSorted = Arrays.copyOf(zList, zi);

        dynamicEntities.removeAll(targets);
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
                newCrash.convertToGlobal(sm);
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
     * @return an unsafe array of the entities. Should only be used for querying, otherwise it must be cloned
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
        return Collections.unmodifiableCollection(dynamicEntities);
    }

    @Override
    public void updateEntities(float currentTime) {
        for (MovingEntity entity : dynamicEntities) {
            entity.update(currentTime);
        }
    }

    @Override
    public void cleanUp() {
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];
        Logger.removeOnlineUpdate(collisionCounter);
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
            PosVector position = entity.getExpectedPosition();
            this.range = entity.getRange();
            x = position.x;
            y = position.y;
            z = position.z;
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
    }
}
