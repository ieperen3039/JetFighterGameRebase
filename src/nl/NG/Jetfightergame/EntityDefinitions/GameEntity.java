package nl.NG.Jetfightergame.EntityDefinitions;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.EntityDefinitions.Hitbox.Collision;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.QuaternionInterpolator;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.VectorInterpolator;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public abstract class GameEntity implements MovingEntity {

    private static final int INTERPOLATION_QUEUE_SIZE = 5;

    /** worldspace position in m */
    protected PosVector position;
    /** worldspace movement in m/s */
    protected DirVector velocity;
    /** absolute rotation */
    protected Quaternionf rotation;
    /** rotation speeds in rad/s */
    protected float yawSpeed, pitchSpeed, rollSpeed;

    /** extrapolated worldspace position */
    protected PosVector extraPosition;
    /** expected rotation */
    protected Quaternionf extraRotation;

    private float drawTimer = -Float.MAX_VALUE;
    private VectorInterpolator positionInterpolator;
    private QuaternionInterpolator rotationInterpolator;

    /** collision of this gametick, null if it doesn't hit */
    protected Extreme<Collision> nextCrash;
    /** cached positions of the hitpoints*/
    private Collection<TrackedVector<PosVector>> hitPoints = null;

    /** worldspace / localspace */
    private float scale;
    private Material surfaceMaterial;
    protected final float mass;

    /**
     * any object that may be moved and hit other objects, is a game object. All vectors are newly instantiated.
     * @param surfaceMaterial material properties
     * @param scale scalefactor applied to this object. the scale is in global space and executed in {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     */
    public GameEntity(Material surfaceMaterial, float mass, float scale, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation) {
        this.position = new PosVector(initialPosition);
        this.extraPosition = new PosVector(initialPosition);
        this.rotation = new Quaternionf(initialRotation);
        this.extraRotation = new Quaternionf(initialRotation);

        this.surfaceMaterial = surfaceMaterial;
        this.scale = scale;
        this.velocity = new DirVector(initialVelocity);
        this.mass = mass;

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        positionInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, position);
        rotationInterpolator = new QuaternionInterpolator(INTERPOLATION_QUEUE_SIZE, rotation);
    }

    @Override
    public void preUpdate(float deltaTime, DirVector netForce) {
        nextCrash = new Extreme<>(false);
        extraPosition = new PosVector();
        extraRotation = new Quaternionf();

        applyPhysics(deltaTime, netForce);

        updateShape(deltaTime);
    }


    /**
     * update the shape of the airplane, if applicable
     */
    protected abstract void updateShape(float deltaTime);

    /**
     * @param relative a vector relative to this object
     * @param ms a frame of reference, the resulting vector will be in the space of the instantiation of this matrix
     * @return a vector in {@code sm}'s frame of reference
     */
    public DirVector relativeToWorldSpace(DirVector relative, MatrixStack ms) {
        final DirVector[] axis = new DirVector[1];

        toLocalSpace(ms, () -> axis[0] = ms.getDirection(relative), position, rotation);
        return axis[0];
    }

    public abstract void applyPhysics(float deltaTime, DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface
     * synchronization with other operations on position and rotation should be synchronized
     * @param currentTime seconds between some startTime t0 and the begin of the current gameloop
     * @param deltaTime time since last update call
     */
    public void update(float currentTime, float deltaTime) {
        hitPoints = null;

        // update velocity
        position.to(extraPosition, velocity).scale(deltaTime, velocity);

        position = extraPosition;
        rotation = extraRotation;

        positionInterpolator.add(position, currentTime);
        rotationInterpolator.add(rotation, currentTime);
    }

    @Override
    public boolean checkCollisionWith(Touchable other){
        Collision newCollision = getHitpointMovement().stream()
                // see which points collide with the world
                .map(point -> getPointCollision(point, other))
                // exclude points that didn't hit
                .filter(Objects::nonNull)
                // select first point hit
                .min(Collision::compareTo)
                // return the final rotation
                .orElse(null);

        // if there is a collision earlier than the currently registered collision
        nextCrash.check(newCollision);

        return newCollision != null;
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame.
     * the returned collision is caused by the first plane hit by point
     * @param point a point in global space
     * @param other another object
     * @return the first collision caused by this point
     */
    private Collision getPointCollision(TrackedVector<PosVector> point, Touchable other) {

        Stream.Builder<Collision> multipliers = Stream.builder();

        // collect the collisions
        final ShadowMatrix identity = new ShadowMatrix();
        final Consumer<Shape> exec = shape -> {
            // map points to local space
            PosVector startPoint = identity.getReversePosition(point.previous());
            PosVector endPoint = identity.getReversePosition(point.current());
            DirVector direction = startPoint.to(endPoint, new DirVector());
            // search hitpoint, add it when found
            Collision stopVec = shape.getMaximumMovement(startPoint, direction, endPoint);
            if (stopVec != null) multipliers.add(stopVec);
        };

        other.toLocalSpace(identity, () -> create(identity , exec, true));

        // iterate over all collisions
        return multipliers.build()
                // select the smallest
                .min(Collision::compareTo)
                .orElse(null);
    }

    /**
     * collects the movement of the hitpoints of this object for the current state, and caches it
     * @return a collection of the positions of the hitpoints in world space
     * TODO optimization
     */
    private Collection<TrackedVector<PosVector>> getHitpointMovement() {
        if (hitPoints == null) {

            final List<PosVector> previous = getCurrentPosition();
            final List<PosVector> current = getNextPosition();

            // combine both lists into one list
            List<TrackedVector<PosVector>> points = new ArrayList<>(previous.size());
            Iterator<PosVector> previousPoints = previous.iterator();
            Iterator<PosVector> currentPoints = current.iterator();
            while (previousPoints.hasNext()) {
                points.add(new TrackedVector<>(previousPoints.next(), currentPoints.next()));
            }
            hitPoints = points;
        }

        return hitPoints;
    }

    // collect the extrapolated position of the points of this object in worldspace
    private List<PosVector> getNextPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> current = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPoints().stream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(current::add)
        );
        toLocalSpace(identity, () -> create(identity, collect, true), true);
        return current;
    }

    // collect the current position of the points of this object in worldspace
    private List<PosVector> getCurrentPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> previous = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPoints().stream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(previous::add)
        );
        toLocalSpace(identity, (() -> create(identity, collect, false)), false);
        return previous;
    }

    public void toLocalSpace(MatrixStack ms, Runnable action, boolean extrapolate) {
        PosVector currPos = extrapolate ? extraPosition : position;
        Quaternionf currRot = extrapolate ? extraRotation : rotation;

        toLocalSpace(ms, action, currPos, currRot);
    }

    private void toLocalSpace(MatrixStack ms, Runnable action, PosVector currPos, Quaternionf currRot) {
        ms.pushMatrix();
        {
            ms.translate(currPos);
            ms.rotate(currRot);
            ms.scale(scale);
            action.run();
        }
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }

    @Override
    public void draw(GL2 gl, float currentTime) {
        PosVector pos = interpolatePosition(currentTime);
        Quaternionf rot = rotationInterpolator.getInterpolated(currentTime);

        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, () -> create(gl, painter), pos, rot);
    }

    /** returns the latest calculated position, but one should preferably use {@link #interpolatePosition(float)}*/
    public PosVector getPosition() {
        return position;
    }

    @Override
    public PosVector interpolatePosition(float currentTime) {
        if (currentTime < drawTimer) throw new IllegalArgumentException(
                "currentTime must be larger than any earlier call of currentTime\n" +
                        "earlier call was " + drawTimer + ", this call was " + currentTime
        );
        drawTimer = currentTime;

        return positionInterpolator.getInterpolated(currentTime).toPosVector();
    }
    
    @Override
    public DirVector getVelocity() {
        return velocity;
    }

    /** an object that represents {@code null}. Making this appear in-game is an achievement */
    public static final GameEntity EMPTY_OBJECT = new GameEntity(Material.GLOWING, 1f, 1f, PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf()) {
        public void create(MatrixStack ms, Consumer<Shape> action, boolean b) {}
        public void draw(GL2 ms) {Toolbox.drawAxisFrame(ms);}
        public void applyCollision() {}
        protected void updateShape(float deltaTime) {}
        public void applyPhysics(float deltaTime, DirVector netForce) {position.add(netForce.scale(deltaTime, new DirVector()), new PosVector());}
    };
}
