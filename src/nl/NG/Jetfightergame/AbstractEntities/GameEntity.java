package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.QuaternionInterpolator;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.VectorInterpolator;
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
    /***/
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
        this.mass = mass;
        renderTime = renderTimer;

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
        extraPosition = new PosVector();
        extraRotation = new Quaternionf();
        extraVelocity = new DirVector();

        // nothing to do when no time is passed
        if (deltaTime > 0) {
            applyPhysics(deltaTime, netForce);
        } else {
            extraRotation.set(rotation);
            extraPosition.set(position);
        }

        updateShape(deltaTime);
    }


    /**
     * update the shape of the airplane, if applicable
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
     * except for {@link #extraPosition}, {@link #extraRotation} and {@link #extraVelocity}
     * @param deltaTime time-difference, cannot be 0
     * @param netForce accumulated external forces on this object
     */
    public abstract void applyPhysics(float deltaTime, DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface
     * synchronization with other operations on position and rotation should be synchronized
     * @param currentTime seconds between some startTime t0 and the begin of the current gameloop
     * @param deltaTime time since last update call
     */
    public void update(float currentTime, float deltaTime) {
        if (position.x() == Float.NaN || position.y() == Float.NaN || position.z() == Float.NaN)
            throw new IllegalStateException("Invalid position of " + toString() + ": " + position.toString());
        if (rotation.x() == Float.NaN || rotation.y() == Float.NaN || rotation.z() == Float.NaN)
            throw new IllegalStateException("Invalid rotation of " + toString() + ": " + rotation.toString());

        hitPoints = null;

        position = extraPosition;
        rotation = extraRotation;
        velocity = extraVelocity;
        positionInterpolator.add(position, currentTime);
        rotationInterpolator.add(rotation, currentTime);
    }

    @Override
    public boolean checkCollisionWith(Touchable other){
        Collision newCollision = getHitpointMovement().stream()
                // see which points collide with the other
                .map(point -> getPointCollision(point, other))
                // exclude points that didn't hit
                .filter(Objects::nonNull)
                // select first point hit
                .min(Collision::compareTo)
                // if there has been no collision, return null
                .orElse(null);

        // update if there is a collision earlier than the currently registered collision
        nextCrash.check(newCollision);

        return newCollision != null;
    }

    @Override
    public void applyCollision(float deltaTime, float previousTimeStamp) {

        if (extraVelocity.length() > 100)
            Toolbox.printSpamless(Integer.toHexString(hashCode()), toString() + "\nmoving " + extraVelocity.length() + " m/s",
                    extraPosition, velocity, deltaTime);

        final DirVector movement = position.to(extraPosition, new DirVector());

        if (nextCrash.get() == null || !movement.isScalable()) {
            return;
        }

        // relative position of contact
        final PosVector globalHitPos = nextCrash.get().hitPos;
        // normal of the plane of contact
        final DirVector contactNormal = nextCrash.get().normal;
        // fraction of deltaTime until collision
        final float timeScalar = nextCrash.get().timeScalar;
        // seconds until this object hits the other
        final float hitDeltaTime = timeScalar * deltaTime;
        // current rotation speed of airplane
        final Vector3f rotationSpeedVector = new Vector3f(rollSpeed, pitchSpeed, yawSpeed);
        // movement until collision
        final DirVector interMovement = movement.scale(timeScalar, new DirVector());
        // object position when hitting
        final PosVector interPosition = position.add(interMovement, new PosVector());
        // object rotation when hitting
        Quaternionf interRotation = rotation.rotate(rollSpeed * hitDeltaTime, pitchSpeed * hitDeltaTime, yawSpeed * hitDeltaTime, new Quaternionf());

        simpleBounceCollision(globalHitPos, contactNormal, rotationSpeedVector, interPosition, extraPosition);

        rollSpeed = rotationSpeedVector.x();
        pitchSpeed = rotationSpeedVector.y();
        yawSpeed = rotationSpeedVector.z();

        interPosition.to(extraPosition, interMovement).reducedTo(extraVelocity.length(), extraVelocity);
        hitPoints = null;

        Toolbox.print(extraPosition, extraVelocity);

        // add intermediate position/rotation to interpolation
        final float collisionTimeStamp = previousTimeStamp + hitDeltaTime;
//        positionInterpolator.add(interPosition, collisionTimeStamp);
//        rotationInterpolator.add(interRotation, collisionTimeStamp);
    }

    /**
     * bounce without rotation
     *
     * @param hitPosition         global position of the point on this object that caused the collision
     * @param contactNormal       the normal of the plane that the point has hit
     * @param rotationSpeedVector vector of rotation, defined as (rollSpeed, pitchSpeed, yawSpeed).
     *                            This should contain the new rotations upon returning
     * @param interPosition       the position where the object is at the moment of collision
     * @param newPosition         the current extrapolated position of this object.
     *                            The new extrapolated position should be stored here upon returning
     */
    private void simpleBounceCollision(PosVector hitPosition, DirVector contactNormal, Vector3f rotationSpeedVector,
                                       PosVector interPosition, PosVector newPosition
    ) {
        final DirVector remainingMovement = interPosition.to(newPosition, new DirVector());
        remainingMovement.reflect(contactNormal);
        interPosition.add(remainingMovement, newPosition);
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

        // collect the collisions
        final ShadowMatrix identity = new ShadowMatrix();
        final Consumer<Shape> exec = shape -> {
            // map point to local space
            PosVector startPoint = identity.mapToLocal(point.previous());
            PosVector endPoint = identity.mapToLocal(point.current());
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
            moving.toLocalSpace(identity, () -> moving.create(identity, exec, true), true);
        } else {
            other.toLocalSpace(identity, () -> other.create(identity, exec));
        }

        // iterate over all collisions
        return collisions.build()
                // select the smallest
                .min(Collision::compareTo)
                .orElse(null);
    }

    /**
     * collects the movement of the hitpoints of this object for the current state, and caches it
     * @return a collection of the positions of the hitpoints in world space
     */
    private Collection<TrackedVector<PosVector>> getHitpointMovement() {
        if (hitPoints == null) {
            // we consider from extrapolated perspective
            final List<PosVector> previous = getCurrentPosition();
            final List<PosVector> current = getNextPosition();

            // combine both lists into one list
            List<TrackedVector<PosVector>> points = new ArrayList<>(previous.size());
            Iterator<PosVector> previousPoints = previous.iterator();
            Iterator<PosVector> currentPoints = current.iterator();
            while (previousPoints.hasNext()) {
                points.add(new TrackedVector<>(currentPoints.next(), previousPoints.next()));
            }
            hitPoints = points;
        }

        return hitPoints;
    }

    // collect the extrapolated position of the points of this object in worldspace
    private List<PosVector> getNextPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> list = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPoints().stream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(list::add)
        );
        toLocalSpace(identity, () -> create(identity, collect, true), true);
        return list;
    }

    // collect the current position of the points of this object in worldspace
    private List<PosVector> getCurrentPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> list = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPoints().stream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(list::add)
        );
        toLocalSpace(identity, () -> create(identity, collect, false), false);
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
        return new PosVector(cachedPosition);
    }

    private Quaternionf interpolatedRotation() {
        updateInterpolationCache();
        return cachedRotation;
    }

    private void updateInterpolationCache() {
        if (cachedTime != renderTime.current()) {
            cachedPosition = positionInterpolator.getInterpolated(cachedTime).toPosVector();
            cachedRotation = rotationInterpolator.getInterpolated(cachedTime);
            cachedTime = renderTime.current();
        }
    }

    @Override
    public DirVector getVelocity() {
        return velocity;
    }

    @Override
    public abstract String toString();
}
