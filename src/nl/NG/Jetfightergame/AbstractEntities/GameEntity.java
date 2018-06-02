package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Interpolation.QuaternionInterpolator;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

import static java.lang.StrictMath.sqrt;

/**
 * @author Geert van Ieperen created on 29-10-2017.
 */
public abstract class GameEntity implements MovingEntity {

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

    /** collision of this gametick, null if it doesn't hit */
    protected Extreme<Collision> nextCrash;
    /** cached positions of the hitpoints */
    private Collection<TrackedVector<PosVector>> hitPoints = null;

    /**
     * The render timer. gameTime.getRenderTime().current() will provide the current time for interpolation, and
     * renderTime.difference() provides the deltaTime
     */
    private GameTimer gameTimer;

    /** worldspace / localspace */
    protected final float scale;
    protected final float mass;

    /**
     * any object that may be moved and hit other objects, is a game object. All vectors are newly instantiated.
     * @param id an unique identifier for this entity
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param mass            the mass of the object in kilograms.
     * @param scale           scalefactor applied to this object. the scale is in global space and executed in {@link
*                        #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param gameTimer       A timer that defines rendering, in order to let {@link MovingEntity#interpolatedPosition()}
     */
    public GameEntity(
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

        resetCache();
    }

    @Override
    public void preUpdate(float deltaTime, DirVector netForce) {

        nextCrash = new Extreme<>(false);

        extraPosition.set(position);
        extraRotation.set(rotation);
        extraVelocity.set(velocity);

        // nothing to do when no time is passed
        if (deltaTime != 0) applyPhysics(netForce, deltaTime);

        updateShape(deltaTime);

        hitPoints = calculateHitpointMovement();
    }


    /**
     * entities may include animations. These animations are updated here.
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
     * @return a vector in {@code sm}'s frame of reference
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

    @Override
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

    @Override
    public boolean checkCollisionWith(Touchable other, float deltaTime) {
        // projectiles cannot be hit
        if (other instanceof Projectile) return false;

        Collision newCollision = hitPoints.stream()
                // see which points collide with the other
                .map(point -> getPointCollision(point, other, deltaTime))
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
        nextCrash.check(new Collision(cause));
    }

    @Override
    public int idNumber() {
        return thisID;
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame. the returned collision is caused by
     * the first plane of #other, as it is hit by #point
     * @param point     the movement of a point in global space
     * @param other     another object
     * @param deltaTime time-difference of this loop
     * @return the first collision caused by this point on the other object
     */
    private Collision getPointCollision(TrackedVector<PosVector> point, Touchable other, float deltaTime) {

        Extreme<Collision> firstHit = new Extreme<>(false);
        // copy previous, because we may want to temporarily override it.
        final PosVector previous = new PosVector(point.previous());
        final PosVector current = point.current();

        // collect the collisions
        final ShadowMatrix sm = new ShadowMatrix();
        final Consumer<Shape> addCollisions = shape -> {
            // map point to local space
            PosVector startPoint = sm.mapToLocal(previous);
            PosVector endPoint = sm.mapToLocal(current);
            DirVector direction = startPoint.to(endPoint, new DirVector());

            // search hitpoint, add it when found
            Collision newCrash = shape.getCollision(startPoint, direction, endPoint);
            if (newCrash != null) {
                newCrash.convertToGlobal(sm);
                firstHit.check(newCrash);
            }
        };

        if (other instanceof MovingEntity) {
            final MovingEntity moving = (MovingEntity) other;

            // consider the movement of the plane, by assuming relative movement and linear interpolation.
            // despite that #previous is as final in Consumer #addCollision, this will still be considered, because the fields are not final
            final DirVector velocity = moving.getVelocity();
            if (velocity.isScalable()) previous.add(velocity.scale(deltaTime, velocity));

            moving.toLocalSpace(sm, () -> moving.create(sm, addCollisions), true);
        } else {
            other.toLocalSpace(sm, () -> other.create(sm, addCollisions));
        }

        return firstHit.get();
    }

    protected List<TrackedVector<PosVector>> calculateHitpointMovement() {
        final List<PosVector> previous = getPointPositions(false);
        final List<PosVector> next = getPointPositions(true);

        // combine both lists into one list
        List<TrackedVector<PosVector>> points = new ArrayList<>(previous.size());
        Iterator<PosVector> previousPoints = previous.iterator();
        Iterator<PosVector> nextPoints = next.iterator();
        while (previousPoints.hasNext()) {
            points.add(new TrackedVector<>(previousPoints.next(), nextPoints.next()));
        }
        return points;
    }

    // collect the position of the points of this object in worldspace
    private List<PosVector> getPointPositions(boolean extrapolate) {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> list = new ArrayList<>();
        final Consumer<Shape> collect = (shape -> shape.getPointStream()
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

    /**
     * returns the latest calculated position for rendering, one should preferably use {@link
     * MovingEntity#interpolatedPosition()}
     */
    public PosVector getPosition() {
        return new PosVector(position);
    }

    @Override
    public PosVector getExpectedPosition() {
        return new PosVector(extraPosition);
    }

    @Override
    public void applyJerk(DirVector direction, float energy, float deltaTime) {
        // e = 0.5*m*v*v
        // >> v*v = e / 0.5m
        // >> v = sqrt(2e / m)
        float v = (float) sqrt((2 * energy) / mass);

        DirVector dv = direction.scale(v, direction);
        extraVelocity.add(dv);
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        hitPoints = calculateHitpointMovement();
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
        positionInterpolator.updateTime(renderTime());
        return positionInterpolator.getInterpolated(renderTime()).toPosVector();
    }

    @Override
    public Quaternionf interpolatedRotation() {
        rotationInterpolator.updateTime(renderTime());
        return rotationInterpolator.getInterpolated(renderTime());
    }

    protected Float renderTime() {
        return gameTimer.getRenderTime().current();
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public DirVector getVelocity() {
        return new DirVector(velocity);
    }

    @Override
    public void addStatePoint(float currentTime, PosVector newPosition, Quaternionf newRotation) {
        position.set(newPosition);
        rotation.set(newRotation);
        positionInterpolator.add(newPosition, currentTime);
        rotationInterpolator.add(newRotation, currentTime);
    }

    @Override
    public Quaternionf getRotation() {
        return rotation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + idNumber() + ")";
    }

    public void resetCache() {
        positionInterpolator = new VectorInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, position);
        rotationInterpolator = new QuaternionInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, rotation);
    }

    /**
     * @return the kinetic energy of this entity in the direction of vector
     */
    @Override
    public float getKineticEnergy(DirVector vector) {
        float leftSpeed = extraVelocity.dot(vector);
        return 0.5f * leftSpeed * leftSpeed * mass;
    }

    protected DirVector renderVelocity() {
        positionInterpolator.updateTime(renderTime());
        return positionInterpolator.getDerivative();
    }

    @Override
    public void elasticCollision() {
        // calculate inverse inertia tensor
        Matrix3f inertTensor = new Matrix3f().scale(1 / mass);
        Matrix3f rotMatrix = rotation.get(new Matrix3f());
        Matrix3f rotInert = rotMatrix.mul(inertTensor, inertTensor);
        Matrix3f rotTranspose = rotMatrix.transpose();
        Matrix3f invInertTensor = rotInert.mul(rotTranspose).invert();

        Collision coll = nextCrash.get();
        if (velocity.dot(coll.normal) < 0) velocity.reflect(coll.normal);

        Vector3f angVelChange = coll.normal.cross(coll.hitPos, new Vector3f());
        invInertTensor.transform(angVelChange);
        angVelChange.add(rollSpeed, pitchSpeed, yawSpeed).mul(0.8f); // *0.8 to prevent objects from extreme spinning
        rollSpeed = angVelChange.x;
        pitchSpeed = angVelChange.y;
        yawSpeed = angVelChange.z;
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

        public State(PosVector firstPos, PosVector secondPos, Quaternionf firstRot, Quaternionf secondRot, DirVector velocity, DirVector forward) {
            this.firstPos = firstPos;
            this.secondPos = secondPos;
            this.firstRot = firstRot;
            this.secondRot = secondRot;
            this.velocity = new DirVector(velocity);
            this.forward = new DirVector(forward);
        }

        public PosVector position(float timeFraction) {
            return firstPos.interpolateTo(secondPos, timeFraction);
        }

        public Quaternionf rotation(float timeFraction) {
            return firstRot.nlerp(secondRot, timeFraction);
        }

        public DirVector velocity() {
            return velocity;
        }

        public DirVector forward() {
            return forward;
        }
    }
}
