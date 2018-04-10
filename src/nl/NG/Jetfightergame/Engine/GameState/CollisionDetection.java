package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.AveragingQueue;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.NG.Jetfightergame.Settings.Settings.DEBUG;
import static nl.NG.Jetfightergame.Settings.Settings.MAX_COLLISION_ITERATIONS;

/**
 * @author Geert van Ieperen
 * created on 10-3-2018.
 */
public class CollisionDetection {
    private List<HUDTargetable> debugs = new LinkedList<>();

    private CollisionEntity[] xLowerSorted;
    private CollisionEntity[] yLowerSorted;
    private CollisionEntity[] zLowerSorted;

    private AveragingQueue avgCollision = new AveragingQueue(20);
    private final Consumer<ScreenOverlay.Painter> collisionCounter = (hud) ->
            hud.printRoll(String.format("Collision pair count average: %1.01f", avgCollision.average()));

    private final Collection<Touchable> staticEntities;
    private Collection<MovingEntity> dynamicClones;

    public CollisionDetection(Collection<MovingEntity> dynamicEntities, Collection<Touchable> staticEntities) {
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
        ScreenOverlay.addHudItem(collisionCounter);

        xLowerSorted = new CollisionEntity[dynamicEntities.size()];
        yLowerSorted = new CollisionEntity[dynamicEntities.size()];
        zLowerSorted = new CollisionEntity[dynamicEntities.size()];

        int i = 0;
        for (MovingEntity newEntity : dynamicEntities) {
            CollisionEntity asCollisionEntity = new CollisionEntity(newEntity);
            xLowerSorted[i] = asCollisionEntity;
            yLowerSorted[i] = asCollisionEntity;
            zLowerSorted[i] = asCollisionEntity;
            i++;
        }

        Arrays.sort(xLowerSorted, (a, b) -> Float.compare(a.xLower(), b.xLower()));
        Arrays.sort(yLowerSorted, (a, b) -> Float.compare(a.yLower(), b.yLower()));
        Arrays.sort(zLowerSorted, (a, b) -> Float.compare(a.zLower(), b.zLower()));

        dynamicClones = Collections.unmodifiableCollection(dynamicEntities);
    }

    public void preUpdateEntities(GameState gameState, float deltaTime) {
        Arrays.stream(entityArray())
//                .parallel()
                .map(e -> e.entity)
                .forEach((entity) -> entity.preUpdate(deltaTime, gameState.entityNetforce(entity)));
    }

    /**
     * checks and resolves all collisions that occurred in the given timeperiod
     * @param currentTime the current game-loop time
     * @param deltaTime the in-game time difference from the last call to this method
     */
    public void analyseCollisions(float currentTime, float deltaTime) {
        int remainingLoops = MAX_COLLISION_ITERATIONS;

        // the pairs that have their collision been processed
        List<Pair<Touchable, MovingEntity>> collisionPairs;

        do {
            /* as a single collision may result in a previously not-intersecting pair to collide,
             * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
             * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
             * On the other hand, we may assume collisions of that magnitude appear seldom
             */
            collisionPairs = getIntersectingPairs().parallelStream()
                    // check for collisions, remove items that did not collide
                    .filter(closeTarget -> checkCollisionPair(closeTarget.right, closeTarget.left, deltaTime))
                    .collect(Collectors.toList());

            // process the final collisions in pairs
            List<RigidBody> postCollisions = collisionPairs.stream()
                    .flatMap(p -> processCollision(deltaTime, p))
                    // every item once
                    .distinct()
                    .collect(Collectors.toList());

            // apply the collisions to the objects
            postCollisions//.parallelStream()
                    .forEach(r -> r.apply(deltaTime, currentTime));

        } while (!collisionPairs.isEmpty() && (--remainingLoops > 0) && !Thread.interrupted());

        //avgCollision.add(newCollisions);

        if (!collisionPairs.isEmpty()) {
            Toolbox.print(collisionPairs.size() + " collisions not resolved");
        }

    }

    private static Stream<? extends RigidBody> processCollision(float deltaTime, Pair<Touchable, MovingEntity> p) {
        RigidBody left = p.left.getFinalCollision(deltaTime);
        RigidBody right = p.right.getFinalCollision(deltaTime);
        // collision response
        RigidBody.process(left, right);
        return Stream.of(left, right);
    }

    /**
     * @return false iff neither hits the other
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private boolean checkCollisionPair(MovingEntity moving, Touchable either, float deltaTime) {
        // the isDead checks are for entities that die in a previous collision iteration
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
        debugs.forEach(HUDTargetable::dispose);
        debugs.clear();

        // select all source pairs that are 'close' in three coordinates
        for (int i = 0; i < nOfEntities; i++) {
            for (int j = i - 1; j > 0; j--) {
                // count how often i hits j and how often j hits i.
                int intervalAgreements = adjacencyMatrix[i][j];

                if (intervalAgreements >= 3){
                    Pair<Touchable, MovingEntity> newPair = new Pair<>(entityArray[i].entity, entityArray[j].entity);
                    allEntityPairs.add(newPair);
                    if (DEBUG) {
//                        debugs.add(new EnemyFlyingTarget(entityArray[i].entity));
//                        debugs.add(new EnemyFlyingTarget(entityArray[j].entity));
                    }
                }
            }
        }

        // add all world-source pairs for now
        for (Touchable worldPart : staticEntities) {
            for (CollisionEntity target : entityArray) {
                allEntityPairs.add(new Pair<>(worldPart, target.entity));
            }
        }

        // run some checks
        if (DEBUG) {
            long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
            if (nulls > 0) Toolbox.print("nulls found in intersecting pairs: " + nulls);
            long equals = allEntityPairs.stream().filter(p -> p.left.equals(p.right)).count();
            if (equals > 0) Toolbox.print("duplicates found in intersecting pairs: " + equals);
        }

        avgCollision.add(allEntityPairs.size());

        return allEntityPairs;
    }

    public void testSort() {
        Toolbox.printSpamless("testSort", "\n" + Toolbox.getCallingMethod(1) + " Testing Sorting");
        float init = -Float.MAX_VALUE;
        for (int i = 0; i < xLowerSorted.length; i++) {
            CollisionEntity collisionEntity = xLowerSorted[i];
            if (collisionEntity.xLower() < init) {
                Toolbox.print("Sorting error on x = " + i);
                for (CollisionEntity entity : xLowerSorted) {
                    System.out.print(entity.xLower() + ", ");
                }
                Toolbox.exitJava();
            }
            init = collisionEntity.xLower();
        }

        init = -Float.MAX_VALUE;
        for (int i = 0; i < yLowerSorted.length; i++) {
            CollisionEntity collisionEntity = yLowerSorted[i];
            if (collisionEntity.yLower() < init) {
                Toolbox.print("Sorting error on y = " + i);
                for (CollisionEntity entity : yLowerSorted) {
                    System.out.print(entity.yLower() + ", ");
                }
                Toolbox.exitJava();
            }
            init = collisionEntity.yLower();
        }

        init = -Float.MAX_VALUE;
        for (int i = 0; i < zLowerSorted.length; i++) {
            CollisionEntity collisionEntity = zLowerSorted[i];
            if (collisionEntity.zLower() < init) {
                Toolbox.print("Sorting error on z = " + i);
                for (CollisionEntity entity : zLowerSorted) {
                    System.out.print(entity.zLower() + ", ");
                }
                Toolbox.exitJava();
            }
            init = collisionEntity.zLower();
        }
    }

    /**
     * iterating over xLowerSorted, increase the value of all pairs that have coinciding intervals
     * @param adjacencyMatrix the matrix where the pairs are marked using entity id's
     * @param sortedArray an array sorted increasingly on the lower mapping
     * @param lower a mapping that maps to the lower value of the interval of the entity
     * @param upper a mapping that maps an entity to its upper interval
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

    /**
     * adds the new entities to the collision detection, and cleans out dead entities from the arrays.
     * @param newEntities a set of new entities to be added to the collision detection. The set is unmodified.
     */
    public void prepareCollision(Collection<MovingEntity> newEntities) {
        int nOfNewEntities = newEntities.size();

        for (CollisionEntity entity : entityArray()) {
            entity.update();
        }

        Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
        Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);
        Toolbox.insertionSort(zLowerSorted, CollisionEntity::zLower);

        CollisionEntity[] newXSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newYSort = new CollisionEntity[nOfNewEntities];
        CollisionEntity[] newZSort = new CollisionEntity[nOfNewEntities];

        if (nOfNewEntities > 0) {
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
        }

        xLowerSorted = Toolbox.mergeAndClean(xLowerSorted, newXSort, CollisionEntity::xLower);
        yLowerSorted = Toolbox.mergeAndClean(yLowerSorted, newYSort, CollisionEntity::yLower);
        zLowerSorted = Toolbox.mergeAndClean(zLowerSorted, newZSort, CollisionEntity::zLower);

        dynamicClones = Arrays.stream(entityArray())
                .map(e -> e.entity)
                .collect(Collectors.toList());
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

        for (CollisionEntity e : entityArray()) {
            MovingEntity entity = e.entity;
            entity.toLocalSpace(sm, () -> entity.create(sm, tracer));
        }

        return firstHit.get();
    }

    /**
     * @return an unsafe array of the entities. Should only be used for querying, otherwise it must be cloned
     */
    private CollisionEntity[] entityArray(){
        return xLowerSorted;
    }

    public Collection<Touchable> getStaticEntities() {
        // is unmodifiable
        return staticEntities;
    }

    public Collection<MovingEntity> getDynamicEntities() {
        return dynamicClones;
    }

    public void updateEntities(float currentTime) {
        for (CollisionEntity e : entityArray()) {
            e.entity.update(currentTime);
        }
    }

    public void cleanUp() {
        xLowerSorted = new CollisionEntity[0];
        yLowerSorted = new CollisionEntity[0];
        zLowerSorted = new CollisionEntity[0];
        ScreenOverlay.removeHudItem(collisionCounter);
    }

    protected class CollisionEntity {
        public final MovingEntity entity;
        public int id;

        public float range;
        private float x;
        private float y;
        private float z;

        public CollisionEntity(MovingEntity source) {
            this.entity = source;
            update();
        }

        public void update(){
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
