package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Rendering.Interpolation.QuaternionInterpolator;
import nl.NG.Jetfightergame.Rendering.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    /** expected velocity */
    protected DirVector extraVelocity;

    private VectorInterpolator positionInterpolator;
    private QuaternionInterpolator rotationInterpolator;

    /** collision of this gametick, null if it doesn't hit */
    protected Extreme<Collision> nextCrash;
    /** cached positions of the hitpoints*/
    private Collection<TrackedVector<PosVector>> hitPoints = null;

    /** the render timer. renderTime.current() will provide the current time for interpolation,
     * and renderTime.difference() provides the deltaTime */
    private TrackedFloat renderTime;

    /** worldspace / localspace */
    private float scale;
    private Material surfaceMaterial;
    protected final float mass;
    private float cachedTime;
    private PosVector cachedPosition;
    private Quaternionf cachedRotation;

    /**
     * any object that may be moved and hit other objects, is a game object. All vectors are newly instantiated.
     *
     * @param surfaceMaterial material properties
     * @param scale           scalefactor applied to this object. the scale is in global space and executed in
     *                        {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param renderTimer     the timer of the rendering, in order to let {@link MovingEntity#interpolatedPosition()} return the position
     *                        interpolated on current render time
     */
    public GameEntity(Material surfaceMaterial, float mass, float scale, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation, TrackedFloat renderTimer) {
        this.position = new PosVector(initialPosition);
        this.extraPosition = new PosVector(initialPosition);
        this.rotation = new Quaternionf(initialRotation);
        this.extraRotation = new Quaternionf(initialRotation);


        this.surfaceMaterial = surfaceMaterial;
        this.scale = scale;
        this.velocity = new DirVector(initialVelocity);
        this.extraVelocity = new DirVector(initialVelocity);
        this.mass = mass;
        this.renderTime = renderTimer;

        cachedTime = renderTimer.current();
        cachedPosition = initialPosition;
        cachedRotation = initialRotation;

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        positionInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, position);
        rotationInterpolator = new QuaternionInterpolator(INTERPOLATION_QUEUE_SIZE, rotation);
    }

    @Override
    public void preUpdate(float deltaTime, DirVector netForce) {

        nextCrash = new Extreme<>(false);

        // nothing to do when no time is passed
        if (deltaTime != 0) {
            applyPhysics(deltaTime, netForce);
        } else {
            extraRotation.set(rotation);
            extraPosition.set(position);
            extraVelocity.set(velocity);
        }

        updateShape(deltaTime);

        hitPoints = calculateHitpointMovement();
    }


    /**
     * entities may include animations. These animations are updated here.
     */
    protected abstract void updateShape(float deltaTime);

    /**
     * translates the given relative vector of this object to the object direction in world-space.
     * This method also considers the scaling of the vector; it can be used for relative positions
     * The returned direction is based on the gamestate, for rendering use {@link #relativeInterpolatedDirection(DirVector)}
     * @param relative a vector relative to this object
     * @return a vector in {@code sm}'s frame of reference
     */
    public DirVector relativeStateDirection(DirVector relative) {
        final DirVector[] axis = new DirVector[1];
        ShadowMatrix sm = new ShadowMatrix();
        toLocalSpace(sm, () -> axis[0] = sm.getDirection(relative), position, rotation);
        return axis[0];
    }

    /**
     * translates the given relative vector of this object to the true direction in world-space.
     * This method also considers the scaling of the vector; it can be used for relative positions
     * The returned direction is based on rendertime interpolated direction, for gamestate changes use {@link #relativeStateDirection(DirVector)}
     * @param relative a vector relative to this object
     * @return a vector in {@code sm}'s frame of reference
     */
    public DirVector relativeInterpolatedDirection(DirVector relative) {
        final DirVector[] axis = new DirVector[1];
        ShadowMatrix sm = new ShadowMatrix();
        toLocalSpace(sm, () -> axis[0] = sm.getDirection(relative), interpolatedPosition(), interpolatedRotation());
        return axis[0];
    }

    /**
     * apply net force on this object and possibly read input. Should not change the current state
     * except for {@link #extraPosition}, {@link #extraRotation} and {@link #extraVelocity}.
     * The current values of {@link #extraPosition}, {@link #extraRotation} and {@link #extraVelocity} are invalid.
     * @param deltaTime time-difference, cannot be 0
     * @param netForce accumulated external forces on this object
     */
    public abstract void applyPhysics(float deltaTime, DirVector netForce);

    @Override
    public void update(float currentTime) {
        position.set(extraPosition);
        rotation.set(extraRotation);
        velocity.set(extraVelocity);
        positionInterpolator.add(new PosVector(position), currentTime);
        rotationInterpolator.add(new Quaternionf(rotation), currentTime);

        if ((position.x() == Float.NaN) || (position.y() == Float.NaN) || (position.z() == Float.NaN))
            throw new IllegalStateException("Invalid position of " + toString() + ": " + position.toString());
        if ((rotation.x() == Float.NaN) || (rotation.y() == Float.NaN) || (rotation.z() == Float.NaN))
            throw new IllegalStateException("Invalid rotation of " + toString() + ": " + rotation.toString());

    }

    @Override
    public boolean checkCollisionWith(Touchable other){
        Collision newCollision = hitPoints.stream()
                // see which points collide with the other
                .map(point -> getPointCollision(point, other))
                // exclude points that didn't hit
                .filter(Objects::nonNull)
                // select first point hit
                .min(Collision::compareTo)
                // if there has been no collision, return null
                .orElse(null);

        if (newCollision == null) return false;

        // update if there is a collision earlier than the currently registered collision
        nextCrash.check(newCollision);
        other.acceptCollision(newCollision);

        return true;
    }

    @Override
    public void acceptCollision(Collision cause) {
        nextCrash.check(cause.inverse());
    }

    @Override
    public RigidBody getFinalCollision(float deltaTime) {
        if (deltaTime < 0f)
            throw new RuntimeException("collisions of " + this + " added up to a moment in the future. Running " + -deltaTime + "behind");
        if (nextCrash.get() == null)
            throw new RuntimeException("tried calculating collision of " + this + " even though it did not collide");

        final DirVector movement = position.to(extraPosition, new DirVector());


        // global position of contact
        final PosVector globalHitPos = nextCrash.get().hitPos;
        // normal of the plane of contact
        final DirVector contactNormal = nextCrash.get().normal;
        // fraction of deltaTime until collision
        final float timeScalar = nextCrash.get().timeScalar * 0.99f;
        // seconds until this object hits the other
//        final float hitDeltaTime = timeScalar * deltaTime;
        // current rotation speed of airplane
        final Vector3f rotationSpeedVector = new Vector3f(rollSpeed, pitchSpeed, yawSpeed);
        // object rotation when hitting
        Quaternionf interRotation = new Quaternionf();
        rotation.nlerp(extraRotation, timeScalar, interRotation);
        // movement until collision
        final DirVector interMovement = new DirVector();
        if (!movement.isScalable()) movement.scale(timeScalar, movement);
        // object position when hitting
        final PosVector interPosition = position.add(interMovement, new PosVector());
        // interpolated velocity when hitting
        final DirVector interVelocity = new DirVector();
        interVelocity.set(extraVelocity);
//        velocity.add(velocity.to(extraVelocity, interVelocity).scale(timeScalar, interVelocity), interVelocity);

        return new RigidBody(timeScalar, interPosition, interVelocity, globalHitPos,
                contactNormal, rotationSpeedVector, interRotation, this);
    }

    @Override
    public void applyCollision(RigidBody newState, float deltaTime, float currentTime){
        rollSpeed = newState.rollSpeed();
        pitchSpeed = newState.pitchSpeed();
        yawSpeed = newState.yawSpeed();
        extraVelocity.set(newState.velocity);

        if (newState.isFinal()) return;

        final float remainingTime = deltaTime * (1 - newState.timeScalar);
        newState.massCenterPosition.add(extraVelocity.scale(remainingTime, new DirVector()), extraPosition);
        newState.rotation.rotate(rollSpeed * remainingTime, pitchSpeed * remainingTime, yawSpeed * remainingTime, extraRotation);

        // add intermediate position/rotation to interpolation
        final float collisionTimeStamp = currentTime - remainingTime;
        positionInterpolator.add(new PosVector(extraPosition), collisionTimeStamp);
        rotationInterpolator.add(new Quaternionf(extraRotation), collisionTimeStamp);

        hitPoints = calculateHitpointMovement();
        nextCrash = new Extreme<>(false);
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame.
     * the returned collision is caused by the first plane hit by point
     * @param point a point in global space
     * @param other another object
     * @return the first collision caused by this point
     */
    private Collision getPointCollision(TrackedVector<PosVector> point, Touchable other) {

        Stream.Builder<Collision> collisions = Stream.builder();
        final PosVector previous = point.previous();
        final PosVector current = point.current();

        // collect the collisions
        final ShadowMatrix identity = new ShadowMatrix();
        final Consumer<Shape> addCollisions = shape -> {
            // map point to local space
            PosVector startPoint = identity.mapToLocal(previous);
            PosVector endPoint = identity.mapToLocal(current);
            DirVector direction = startPoint.to(endPoint, new DirVector());
            // search hitpoint, add it when found
            Collision newCrash = shape.getCollision(startPoint, direction, endPoint);
            if (newCrash != null) {
                newCrash.convertToGlobal(identity);
                collisions.add(newCrash);
            }
        };

        if (other instanceof MovingEntity){
            final MovingEntity moving = (MovingEntity) other;
            // consider the movement of the plane, by assuming relative movement and linear interpolation
            previous.add(moving.getVelocity());
            moving.toLocalSpace(identity, () -> moving.create(identity, addCollisions), true);
        } else {
            other.toLocalSpace(identity, () -> other.create(identity, addCollisions));
        }

        // iterate over all collisions
        return collisions.build()
                // select the smallest
                .min(Collision::compareTo)
                .orElse(null);
    }

    private List<TrackedVector<PosVector>> calculateHitpointMovement() {
        // we consider from extrapolated perspective
        final List<PosVector> previous = getPointPositions(false);
        final List<PosVector> current = getPointPositions(true);

        // combine both lists into one list
        List<TrackedVector<PosVector>> points = new ArrayList<>(previous.size());
        Iterator<PosVector> previousPoints = previous.iterator();
        Iterator<PosVector> currentPoints = current.iterator();
        while (previousPoints.hasNext()) {
            points.add(new TrackedVector<>(previousPoints.next(), currentPoints.next()));
        }
        return points;
    }

    // collect the position of the points of this object in worldspace
    private List<PosVector> getPointPositions(boolean extrapolate) {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> list = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPoints().stream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(list::add)
        );
        toLocalSpace(identity, () -> create(identity, collect), extrapolate);
        return list;
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

    /** returns the latest calculated position
     * for rendering, one should preferably use {@link MovingEntity#interpolatedPosition()}
     */
    public PosVector getPosition() {
        return new PosVector(position);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }

    @Override
    public void draw(GL2 gl) {
        PosVector pos = interpolatedPosition();
        Quaternionf rot = interpolatedRotation();

        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, () -> create(gl, painter), pos, rot);
    }

    @Override
    public PosVector interpolatedPosition() {
        updateInterpolationCache();
        return cachedPosition;
    }

    @Override
    public Quaternionf interpolatedRotation() {
        updateInterpolationCache();
        return cachedRotation;
    }

    private void updateInterpolationCache() {
        final float newTime = renderTime.current();
        if ((newTime > 0) && (cachedTime != newTime)) {
            cachedPosition = positionInterpolator.getInterpolated(cachedTime).toPosVector();
            cachedRotation = rotationInterpolator.getInterpolated(cachedTime);
            cachedTime = newTime;
        }
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public DirVector getVelocity() {
        return velocity;
    }

    @Override
    public String toString(){
        return getClass().getSimpleName();
    }
}
