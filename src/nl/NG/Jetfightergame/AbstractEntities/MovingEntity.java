package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Interpolation.QuaternionInterpolator;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.StrictMath.sqrt;

/**
 * @author Geert van Ieperen created on 29-10-2017.
 */
public abstract class MovingEntity implements Touchable {
    private final float spawnTime;
    /** particles and new entities should be passed to this object */
    protected transient SpawnReceiver entityDeposit;
    private final int thisID;

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
    /** cached positions of the hitpoints */
    private PairList<PosVector, PosVector> hitPoints = null;

    /**
     * The render timer. gameTime.getRenderTime().current() will provide the current time for interpolation, and
     * renderTime.difference() provides the deltaTime
     */
    protected GameTimer gameTimer;

    /** worldspace / localspace */
    protected final float scale;
    protected final float mass;

    /**
     * any object that may be moved and may hit other objects, is a game object. All vectors are newly instantiated.
     * @param id              an unique type for this entity
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param mass            the mass of the object in kilograms.
     * @param scale           scale factor applied to this object. The scale is in global space and executed in {@link
     *                        #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param gameTimer       A timer that defines rendering, for use in {@link MovingEntity#interpolatedPosition()}
     */
    public MovingEntity(
            int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
            float mass, float scale, GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        this.thisID = id;
        this.position = new PosVector(initialPosition);
        this.extraPosition = new PosVector(initialPosition);
        this.rotation = new Quaternionf(initialRotation);
        this.extraRotation = new Quaternionf(initialRotation);
        this.velocity = new DirVector(initialVelocity);
        this.extraVelocity = new DirVector(initialVelocity);

        this.scale = scale;
        this.mass = mass;
        this.gameTimer = gameTimer;
        this.entityDeposit = entityDeposit;

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        spawnTime = gameTimer.time();
        resetCache(spawnTime);
    }

    /**
     * move two entities away from each other
     * @param left      one entity, which has collided with right
     * @param right     another entity, which has collided with left
     * @param deltaTime time difference of this gameloop
     */
    public static void entityCollision(MovingEntity left, MovingEntity right, float deltaTime) {
        DirVector leftToRight = new DirVector();
        leftToRight = left.extraPosition.to(right.extraPosition, leftToRight);
        DirVector rightToLeft = leftToRight.negate(new DirVector());

        left.collideWith(right, deltaTime, rightToLeft);
        right.collideWith(left, deltaTime, leftToRight);
    }

    /** @see #entityCollision(MovingEntity, MovingEntity, float) */
    private void collideWith(MovingEntity other, float deltaTime, DirVector otherToThis) {
        float dotProduct = extraVelocity.sub(other.extraVelocity, new DirVector()).dot(otherToThis);
//        dotProduct = Math.abs(dotProduct);
        float scalarLeft = (2 * other.mass / (mass + other.mass)) * (dotProduct / otherToThis.lengthSquared());
        extraVelocity.sub(otherToThis.mul(scalarLeft));
        recalculateMovement(deltaTime);
    }

    /**
     * calculate expected position and rotation, but does not change the current state of the object. This means that
     * {@code rotation} and {@code position} are not updated
     * @param deltaTime time since last frame
     * @param netForce  the net external forces on this object
     */
    public void preUpdate(float deltaTime, DirVector netForce) {
        extraPosition.set(position);
        extraRotation.set(rotation);
        extraVelocity.set(velocity);

        // nothing to do when no time is passed
        if (deltaTime != 0) applyPhysics(netForce, deltaTime);

        updateShape(deltaTime);

        hitPoints = calculateHitpointMovement();
    }

    /**
     * Updates the state of possible animations
     */
    protected abstract void updateShape(float deltaTime);

    /**
     * translates the given relative vector of this object to the object direction in world-space. This method also
     * considers the scaling of the vector; it can be used for relative positions The returned direction is based on the
     * gamestate, for rendering use {@link #relativeInterpolatedDirection(DirVector)}
     * @param relative a vector relative to this object
     * @return a vector in world-space
     */
    public DirVector relativeStateDirection(DirVector relative) {
        final DirVector[] axis = new DirVector[1];
        ShadowMatrix sm = new ShadowMatrix();
        toLocalSpace(sm, () -> axis[0] = sm.getDirection(relative), position, rotation);
        return axis[0];
    }

    /**
     * translates the given relative vector of this object to the true direction in world-space. This method also
     * considers the scaling of the vector; it can be used for relative positions The returned direction is based on
     * rendertime interpolated direction, for gamestate changes use {@link #relativeStateDirection(DirVector)}
     * @param relative a vector relative to this object
     * @return a vector in the current frame of reference
     */
    public DirVector relativeInterpolatedDirection(DirVector relative) {
        final DirVector[] axis = new DirVector[1];
        ShadowMatrix sm = new ShadowMatrix();
        toLocalSpace(sm, () -> axis[0] = sm.getDirection(relative), interpolatedPosition(), interpolatedRotation());
        return axis[0];
    }

    /**
     * apply net force on this object and possibly read input. Should not change the current state except for {@link
     * #extraPosition}, {@link #extraRotation} and {@link #extraVelocity}. The current values of {@link #extraPosition},
     * {@link #extraRotation} and {@link #extraVelocity} are invalid.
     * @param netForce  accumulated external forces on this object
     * @param deltaTime time-difference, cannot be 0
     */
    public abstract void applyPhysics(DirVector netForce, float deltaTime);

    /**
     * update the state of this object, may not be called by any method from another interface. Other operations on
     * position and rotation should be synchronized
     * @param currentTime seconds between some starttime t0 and the begin of the current gameloop
     */
    public void update(float currentTime) {
        velocity.set(extraVelocity);
        addStatePoint(currentTime, extraPosition, extraRotation);

        if (ServerSettings.DEBUG) {
            if ((position.x() == Float.NaN) || (position.y() == Float.NaN) || (position.z() == Float.NaN))
                throw new IllegalStateException("Invalid position of " + toString() + ": " + position.toString());
            if ((rotation.x() == Float.NaN) || (rotation.y() == Float.NaN) || (rotation.z() == Float.NaN))
                throw new IllegalStateException("Invalid rotation of " + toString() + ": " + rotation.toString());
        }
    }

    /**
     * checks the movement of the hitpoints of this object against the planes of 'other'. This method may not change the
     * state of this or of other
     * @param other     an object that may hit this object
     * @param deltaTime
     * @return true if there was a collision. This also means that the other has a collision as well
     */
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        // projectiles cannot be hit
        if (other instanceof AbstractProjectile) return null;

        Collision best = null;
        for (int i = 0; i < hitPoints.size(); i++) {
            Collision newCollision = getPointCollision(hitPoints.left(i), hitPoints.right(i), other, deltaTime);
            if (newCollision != null && (best == null || newCollision.compareTo(best) < 0)) {
                best = newCollision;
            }
        }

        if (best == null) return null;

        other.acceptCollision(best);
        // if the other is a spectral, pretend it didn't hit
        if (other instanceof Spectral) return null;

        return best;
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame. the returned collision is caused by
     * the first plane of #other, as it is hit by #point
     * @param lastPosition   the position of this point at the last game-loop
     * @param next the expected position of this point at the current game loop
     * @param other     another object
     * @param deltaTime time-difference of this loop
     * @return the first collision caused by this point on the other object
     */
    private Collision getPointCollision(PosVector lastPosition, PosVector next, Touchable other, float deltaTime) {
        Extreme<Collision> firstHit = new Extreme<>(false);

        // copy, because we may want to change it
        PosVector previous = new PosVector(lastPosition);

        // collect the collisions
        final ShadowMatrix sm = new ShadowMatrix();
        final Consumer<Shape> addCollisions = shape -> {
            // map point to local space
            PosVector startPoint = sm.mapToLocal(previous);
            PosVector endPoint = sm.mapToLocal(next);
            DirVector direction = startPoint.to(endPoint, new DirVector());

            // search hitpoint, add it when found
            Collision newCrash = shape.getCollision(startPoint, direction, endPoint);
            if (newCrash != null) {
                newCrash.convertToGlobal(sm, this);
                firstHit.check(newCrash);
            }
        };

        if (other instanceof MovingEntity) {
            final MovingEntity moving = (MovingEntity) other;

            // consider the movement of the plane, by assuming relative movement and linear interpolation.
            final DirVector velocity = moving.getVelocity();
            if (velocity.isScalable()) previous.add(velocity.scale(deltaTime));

            moving.toLocalSpace(sm, () -> moving.create(sm, addCollisions), true);
        } else {
            other.toLocalSpace(sm, () -> other.create(sm, addCollisions));
        }

        return firstHit.get();
    }

    protected PairList<PosVector, PosVector> calculateHitpointMovement() {
        final List<PosVector> previous = getPointPositions(false);
        final List<PosVector> next = getPointPositions(true);

        // combine both lists into one list
        PairList<PosVector, PosVector> points = new PairList<>(next.size());
        points.addAll(previous, next);
        return points;
    }

    // collect the position of the points of this object in worldspace
    private List<PosVector> getPointPositions(boolean extrapolate) {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> list = new ArrayList<>();

        final Consumer<Shape> collect = shape -> shape.getPointStream()
                // map to world-coordinates
                .map(identity::getPosition)
                // collect them in an array list
                .forEach(list::add);
        toLocalSpace(identity, () -> create(identity, collect), extrapolate);
        return list;
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        toLocalSpace(ms, action, false);
    }

    /**
     * moves the reference frame from global space to this object and executes action. every create call should preserve
     * the matrix stack.
     * @param ms          reference frame to perform transformations on
     * @param action      action to perform one in local space
     * @param extrapolate true if estimations may be used (e.g. the not-rendered part) false if the actions must be
     *                    performed on parameters that no longer change
     */
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

    /**
     * @return a copy of the position of the center of mass of this object in world-space
     */
    public PosVector getPosition() {
        return new PosVector(position);
    }

    /**
     * applies a change in velocity by applying the given momentum to the velocity. This may only be applied between
     * calls to {@link #preUpdate(float, DirVector)} and {@link #update(float)}
     * @param direction the normalized direction in which the force is applied
     * @param energy    the energy in Newton to be applied to the gravity center of this entity
     * @param deltaTime time difference of the current gameloop
     */
    public void applyJerk(DirVector direction, float energy, float deltaTime) {
        // e = 0.5*m*v*v
        // >> v*v = e / 0.5m
        // >> v = sqrt(2e / m)
        float v = (float) sqrt((2 * energy) / mass);

        DirVector dv = direction.scale(v, direction);
        extraVelocity.add(dv);
        recalculateMovement(deltaTime);
    }

    private void recalculateMovement(float deltaTime) {
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        hitPoints = calculateHitpointMovement();
    }

    @Override
    public void draw(GL2 gl) {
        if (renderTime() < spawnTime) return;

        PosVector pos = interpolatedPosition();
        Quaternionf rot = interpolatedRotation();

        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, () -> create(gl, painter), pos, rot);
    }

    /**
     * @return a copy of the position of the center of mass of this object, interpolated for a predefined timeStamp
     * @implNote The entity should take care of defining a valid timestamp method to ensure correct
     *         interpolation
     */
    public PosVector interpolatedPosition() {
        positionInterpolator.updateTime(renderTime());
        return positionInterpolator.getInterpolated(renderTime()).toPosVector();
    }

    /**
     * @return a copy of the rotation of this object, interpolated for a predefined timeStamp
     * @implNote The entity should take care of defining a valid timestamp method to ensure correct
     *         interpolation
     */
    public Quaternionf interpolatedRotation() {
        rotationInterpolator.updateTime(renderTime());
        return rotationInterpolator.getInterpolated(renderTime());
    }

    protected Float renderTime() {
        return gameTimer.getRenderTime().current();
    }

    /**
     * @return the mass of this object in kilograms.
     */
    public float getMass() {
        return mass;
    }

    /**
     * @return a copy of the movement of the center of mass of this object in world-space
     */
    public DirVector getVelocity() {
        return new DirVector(velocity);
    }

    /** adds a position state for rendering on the specified time */
    public void addStatePoint(float currentTime, PosVector newPosition, Quaternionf newRotation) {
        position.set(newPosition);
        rotation.set(newRotation);
        positionInterpolator.add(newPosition, currentTime);
        rotationInterpolator.add(newRotation, currentTime);
    }

    /**
     * @return the object's current rotation
     */
    public Quaternionf getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + idNumber() + ")";
    }

    private void resetCache(float currentTime) {
        PosVector postPosition = position.add(velocity, new PosVector());

        positionInterpolator = new VectorInterpolator(
                ServerSettings.INTERPOLATION_QUEUE_SIZE,
                position, currentTime,
                postPosition, currentTime + 1
        );

        rotationInterpolator = new QuaternionInterpolator(
                ServerSettings.INTERPOLATION_QUEUE_SIZE,
                rotation, currentTime
        );
    }

    /**
     * @param vector a normalized vector
     * @return the kinetic energy of this entity over the axis given by the vector
     */
    public float getKineticEnergy(DirVector vector) {
        float leftSpeed = extraVelocity.dot(vector);
        return leftSpeed * leftSpeed * mass;
    }

    protected DirVector velocityAtRenderTime() {
        positionInterpolator.updateTime(renderTime());
        return positionInterpolator.getDerivative();
    }

    /**
     * reacts on the given collision as a full elastic collision
     * @param deltaTime
     * @param collision
     */
    public void terrainCollision(float deltaTime, Collision collision) {
        // calculate inverse inertia tensor
//        Matrix3f inertTensor = new Matrix3f().scale(1 / mass);
//        Matrix3f rotMatrix = rotation.get(new Matrix3f());
//        Matrix3f rotInert = rotMatrix.mul(inertTensor, inertTensor);
//        Matrix3f rotTranspose = rotMatrix.transpose();
//        Matrix3f invInertTensor = rotInert.mul(rotTranspose).invert();

        DirVector contactNormal = collision.normal();
        PosVector hitPosition = collision.hitPosition();

        if (extraVelocity.dot(contactNormal) < 0) {
            extraVelocity.reflect(contactNormal);
        }
//        Vector3f angularVelChange = contactNormal.cross(hitPosition, new Vector3f());
//        invInertTensor.transform(angularVelChange);
//        angularVelChange.mul(0.8f);
//
//        rollSpeed += angularVelChange.x;
//        pitchSpeed += angularVelChange.y;
//        yawSpeed += angularVelChange.z;

        recalculateMovement(deltaTime);
    }

    /** @return an unique number given by the server */
    public int idNumber() {
        return thisID;
    }

    /**
     * set the state of this plane to the given parameters. This also updates the interpolation cache, which may result
     * in temporal visual glitches. Usage is preferably restricted to switching worlds
     */
    public void set(PosVector newPosition, DirVector newVelocity, Quaternionf newRotation, float currentTime) {
        this.position = new PosVector(newPosition);
        this.extraPosition = new PosVector(newPosition);
        this.rotation = new Quaternionf(newRotation);
        this.extraRotation = new Quaternionf(newRotation);
        this.velocity = new DirVector(newVelocity);
        this.extraVelocity = new DirVector(newVelocity);

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        resetCache(currentTime);
    }

    public abstract EntityFactory getFactory();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MovingEntity) {
            MovingEntity mov = (MovingEntity) obj;
            return mov.idNumber() == this.idNumber();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return idNumber() >> 3; // for spreading in hashtables
    }

    public static class State {
        private final PosVector firstPos;
        private final PosVector secondPos;
        private final Quaternionf firstRot;
        private final Quaternionf secondRot;
        private final DirVector velocity;
        private final DirVector forward;

        public State() {
            this(
                    PosVector.zeroVector(),
                    PosVector.zeroVector(),
                    new Quaternionf(),
                    new Quaternionf(),
                    DirVector.zeroVector(),
                    DirVector.xVector()
            );
        }

        public State(PosVector position, DirVector direction, DirVector velocity) {
            this(position, Toolbox.xTo(direction), velocity, direction);
        }

        public State(PosVector firstPos, PosVector secondPos, Quaternionf firstRot, Quaternionf secondRot, DirVector velocity, DirVector forward) {
            this.firstPos = firstPos;
            this.secondPos = secondPos;
            this.firstRot = firstRot;
            this.secondRot = secondRot;
            this.velocity = new DirVector(velocity);
            this.forward = new DirVector(forward);
        }

        public State(PosVector position, Quaternionf rotation, DirVector velocity, DirVector forward) {
            this(position, position, rotation, rotation, velocity, forward);
        }

        public PosVector position(float timeFraction) {
            if (timeFraction == 0) return new PosVector(firstPos);
            else return firstPos.interpolateTo(secondPos, timeFraction);
        }

        public Quaternionf rotation(float timeFraction) {
            if (timeFraction == 0) return new Quaternionf(firstRot);
            else return firstRot.nlerp(secondRot, timeFraction);
        }

        public DirVector velocity() {
            return velocity;
        }

        public DirVector forward() {
            return forward;
        }
    }
}
