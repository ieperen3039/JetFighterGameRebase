package nl.NG.Jetfightergame.EntityGeneral;

import nl.NG.Jetfightergame.Assets.Entities.AbstractProjectile;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Interpolation.QuaternionInterpolator;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Float.isInfinite;
import static java.lang.StrictMath.sqrt;

/**
 * @author Geert van Ieperen created on 29-10-2017.
 */
public abstract class MovingEntity implements Touchable {
    private final float spawnTime;
    /** particles and new entities should be passed to this object */
    protected SpawnReceiver entityDeposit;
    private final int thisID;

    /** worldspace position in m */
    protected PosVector position;
    /** worldspace movement in m/s */
    protected DirVector velocity;
    /** absolute rotation */
    protected Quaternionf rotation;
    /** rotation speeds in rad/s */
    protected float yawSpeed, pitchSpeed, rollSpeed;
    protected final float mass;

    /** extrapolated worldspace position */
    protected PosVector extraPosition;
    /** expected rotation */
    protected Quaternionf extraRotation;
    /** expected firstVel */
    protected DirVector extraVelocity;

    protected PairList<Float, Supplier<DirVector>> tempForces;

    protected VectorInterpolator positionInterpolator;
    private QuaternionInterpolator rotationInterpolator;
    /** cached positions of the hitpoints */
    private PairList<PosVector, PosVector> hitPoints = null;

    /**
     * The render timer. gameTime.getRenderTime().current() will provide the current time for interpolation, and
     * renderTime.difference() provides the deltaTime
     */
    protected GameTimer gameTimer;

    /**
     * any object that may be moved and may hit other objects, is a game object. All vectors are newly instantiated.
     * @param id              an unique type for this entity
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param mass            the mass of the object in kilograms.
     * @param gameTimer       A timer that defines rendering, for use in {@link MovingEntity#interpolatedPosition()}
     */
    public MovingEntity(
            int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
            float mass, GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        this.thisID = id;
        this.position = new PosVector(initialPosition);
        this.extraPosition = new PosVector(initialPosition);
        this.rotation = new Quaternionf(initialRotation);
        this.extraRotation = new Quaternionf(initialRotation);
        this.velocity = new DirVector(initialVelocity);
        this.extraVelocity = new DirVector(initialVelocity);

        this.mass = (mass <= 0) ? Float.POSITIVE_INFINITY : mass;
        this.gameTimer = gameTimer;
        this.entityDeposit = entityDeposit;

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        tempForces = new PairList<>();
        spawnTime = gameTimer.time();
        resetCache(spawnTime);
    }

    /**
     * calculate expected position and rotation, but does not change the current state of the object. This means that
     * {@code rotation} and {@code position} are not updated
     * @param netForce  the net external forces on this object
     */
    public void preUpdate(DirVector netForce) {
        extraPosition.set(position);
        extraRotation.set(rotation);
        extraVelocity.set(velocity);

        DirVector force = new DirVector(netForce);

        float time = gameTimer.time();
        float deltaTime = gameTimer.getGameTime().difference();
        for (int i = 0; i < tempForces.size(); i++) {
            if (tempForces.left(i) < time) tempForces.remove(i);
            else force.add(tempForces.right(i).get());
        }

        // nothing to do when no time is passed
        if (deltaTime != 0) applyPhysics(force);

        updateShape(deltaTime);
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
     * #extraPosition}, {@link #extraRotation} and {@link #extraVelocity}.
     * @param netForce  accumulated external forces on this object
     * @implNote The current values of {@link #extraPosition}, {@link #extraRotation} and {@link #extraVelocity}
     *         are invalid.
     */
    public abstract void applyPhysics(DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface. Other operations on
     * position and rotation should be synchronized
     * @param currentTime seconds between some starttime t0 and the begin of the current gameloop
     */
    public void update(float currentTime) {
        if (extraVelocity.isRegular()) {
            velocity.set(extraVelocity);
        } else Logger.WARN.print("Ignored invalid velocity of " + this + " " + extraVelocity);
        if (extraPosition.isRegular()) {
            position.set(extraPosition);
        } else Logger.WARN.print("Ignored invalid position of " + this + " " + extraPosition);
        if (Toolbox.isValidQuaternion(extraRotation)) {
            rotation.set(extraRotation);
        } else Logger.WARN.print("Ignored invalid rotation of " + this + " " + extraRotation);
        hitPoints = null;
    }

    private void validate(String name, Vector vector) {
        if (!vector.isRegular()) {
            throw new IllegalStateException("Invalid " + name + " of " + toString() + ": " + vector.toString());
        }
    }

    /**
     * checks the movement of the hitpoints of this object against the planes of 'other'. This method may not change the
     * state of this or of other
     * @param other     an object that may hit this object
     * @param deltaTime
     * @return the resulting collision, or null if there was none. If a collision is returned, then {@link
     *         #acceptCollision(Collision)} is called on the other as well
     */
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        // projectiles and shields cannot be hit
        if (other instanceof AbstractProjectile) return null;

        Collision best = null;
        PairList<PosVector, PosVector> hitPoints = getHitpoints();
        for (int i = 0; i < hitPoints.size(); i++) {
            Collision newCollision = getPointCollision(this, other, hitPoints.left(i), hitPoints.right(i), deltaTime);
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

    private PairList<PosVector, PosVector> getHitpoints() {
        if (hitPoints == null) hitPoints = calculateHitpointMovement();
        return hitPoints;
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame. the returned collision is caused by
     * the first plane of #other, as it is hit by #point
     * @param source the entity causing the collision
     * @param target        the entity hit by source
     * @param startPosition the position of this point at the last game-loop
     * @param endPosition         the expected position of this point at the current game loop
     * @param deltaTime    time-difference of this loop
     * @return the first collision caused by this point on the other object
     */
    public static Collision getPointCollision(
            MovingEntity source, Touchable target,
            PosVector startPosition, PosVector endPosition,
            float deltaTime
    ) {
        Collision[] firstHit = new Collision[1];

        // copy, because we may want to change it
        PosVector startPosCopy = new PosVector(startPosition);

        // collect the collisions
        final ShadowMatrix sm = new ShadowMatrix();
        final Consumer<Shape> addCollisions = shape -> {
            // map point to local space
            PosVector startPoint = sm.mapToLocal(startPosCopy);
            PosVector endPoint = sm.mapToLocal(endPosition);
            DirVector direction = startPoint.to(endPoint, new DirVector());

            // search hitpoint, add it when found
            Collision newCrash = shape.getCollision(startPoint, direction, endPoint);
            if (newCrash != null) {
                newCrash.convertToGlobal(sm, source);
                Collision min = firstHit[0];
                if (min == null || newCrash.compareTo(min) < 0) {
                    firstHit[0] = newCrash;
                }
            }
        };

        if (target instanceof MovingEntity) {
            final MovingEntity moving = (MovingEntity) target;

            // consider the movement of the plane, by assuming relative movement and linear interpolation.
            final DirVector velocity = moving.getVelocity();
            if (velocity.isScalable()) startPosCopy.add(velocity.scale(deltaTime));

            moving.toLocalSpace(sm, () -> moving.create(sm, addCollisions), true);
        } else {
            target.toLocalSpace(sm, () -> target.create(sm, addCollisions));
        }

        return firstHit[0];
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

    protected void toLocalSpace(MatrixStack ms, Runnable action, PosVector currPos, Quaternionf currRot) {
        ms.pushMatrix();
        {
            ms.translate(currPos);
            ms.rotate(currRot);
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
     * applies a change in firstVel by applying the given momentum to the firstVel. This may only be applied between
     * calls to {@link #preUpdate(DirVector)} and {@link #update(float)}
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

    /** adds a position state for rendering on the specified time */
    public void addStatePoint(float currentTime, PosVector newPosition, Quaternionf newRotation) {
        position.set(newPosition);
        rotation.set(newRotation);

        positionInterpolator.add(newPosition, currentTime);
        rotationInterpolator.add(newRotation, currentTime);
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

    public EntityState getState() {
        return new EntityState(position, extraPosition, rotation, extraRotation, velocity, extraVelocity);
    }

    /**
     * @return the object's current rotation
     */
    public Quaternionf getRotation() {
        return new Quaternionf(rotation);
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

    /**
     * move two entities away from each other
     * @param left      one entity, which has collided with right
     * @param right     another entity, which has collided with left
     * @param deltaTime time difference of this gameloop
     * @param collision the collision of this pair
     */
    public static void entityCollision(MovingEntity left, MovingEntity right, float deltaTime, Collision collision) {

        if (!isInfinite(left.mass) && !isInfinite(right.mass)) {
            PosVector leftPos = left.position.interpolateTo(left.extraPosition, collision.timeScalar);
            PosVector rightPos = right.position.interpolateTo(right.extraPosition, collision.timeScalar);

            DirVector leftToRight = leftPos.to(rightPos, new DirVector());
            if (!leftToRight.isScalable()) return;
            leftToRight.normalize();
            DirVector rightToLeft = new DirVector(leftToRight);
            rightToLeft.negate();

            DirVector leftVel = left.extraVelocity;
            DirVector rightVel = right.extraVelocity;
            left.extraVelocity = left.collideWith(right, rightToLeft, leftVel, rightVel);
            right.extraVelocity = right.collideWith(left, leftToRight, rightVel, leftVel);
        }

        left.recalculateMovement(deltaTime);
        right.recalculateMovement(deltaTime);

        left.validate("position left", left.extraPosition);
        right.validate("position right", right.extraPosition);
    }

    public DirVector getVecTo(MovingEntity other) {
        return position.to(other.position, new DirVector());
    }

    /** @see #entityCollision(MovingEntity, MovingEntity, float, Collision) */
    private DirVector collideWith(MovingEntity other, DirVector otherToThis, DirVector thisVel, DirVector otherVel) {
        DirVector temp = new DirVector();

        float dotProduct = thisVel.sub(otherVel, temp).dot(otherToThis);
        float scalarLeft = ((2 * other.mass) / (this.mass + other.mass));
        float scalarMiddle = dotProduct / otherToThis.lengthSquared();
        thisVel.sub(otherToThis.mul(scalarLeft * scalarMiddle, temp));

        float adjEnergy = (float) Math.sqrt(ServerSettings.BUMPOFF_ENERGY / this.mass);
        thisVel.add(otherToThis.mul(adjEnergy, temp));


        //TODO remove this debug shit
        if (Float.isNaN(thisVel.x) || Float.isNaN(thisVel.y) || Float.isNaN(thisVel.z)) {
            throw new IllegalStateException(toString() + " has NaN velocity " + thisVel + " with other " + other + " has " + otherVel +
                    "\n dot:" + dotProduct + " | sc le:" + scalarLeft + " | sc ri:" + scalarMiddle + " | added e:" + adjEnergy + " | " + otherToThis);
        }
        if (isInfinite(thisVel.x) || isInfinite(thisVel.y) || isInfinite(thisVel.z)) {
            throw new IllegalStateException(toString() + " has Infinite velocity " + thisVel + " with other " + other + " has " + otherVel +
                    "\n dot:" + dotProduct + " | sc le:" + scalarLeft + " | sc ri:" + scalarMiddle + " | added e:" + adjEnergy + " | " + otherToThis);
        }

        return thisVel;
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
//        PosVector hitPosition = collision.hitPosition();

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

    protected DirVector velocityAtRenderTime() {
        positionInterpolator.updateTime(renderTime());
        return positionInterpolator.getDerivative();
    }

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
        return idNumber() ^ Float.hashCode(spawnTime) ^ Float.hashCode(mass); // for spreading in hashtables
    }

    /**
     * the target that is most in front of the user, whatever feels right
     * @param forward  a vector pointing in the direction of a target
     * @param position the position of this
     * @param entities the entities of the game
     * @return one entity from entities which is roughly in front
     */
    public MovingEntity getTarget(DirVector forward, PosVector position, EntityMapping entities) {
        float min = -1;
        MovingEntity tgt = null;

        for (MovingEntity entity : entities) {
            if (entity == this || entity instanceof AbstractProjectile || entity instanceof PowerupEntity) continue;

            Vector3f relPos = entity.getPosition().sub(position).normalize();
            float dot = forward.dot(relPos);

            if (dot > min) {
                min = dot;
                tgt = entity;
            }
        }
        return tgt;
    }

    public void addNetForce(float duration, Supplier<DirVector> localNetForce) {
        tempForces.add(gameTimer.time() + duration, localNetForce);
    }
}
