package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static nl.NG.Jetfightergame.Settings.COLLISION_RESPONSE_LEVEL;

/**
 * a class that represents the collision state of a Touchable Entity.
 * May be considered a subclass of Touchable / GameEntity
 * @author Geert van Ieperen
 */
public class RigidBody {
    public final float timeScalar;
    public final PosVector massCenterPosition;
    public final PosVector hitPosition;
    public final DirVector contactNormal;
    public final Vector3f rotationSpeedVector;
    public final Quaternionf rotation;
    public final Touchable source;
    public final float mass;
    public final DirVector velocity;
    private final Matrix3f invInertTensor;

    /**
     * represents a static body
     * @param hitPosition         global position of the point on this object that caused the collision
     * @param contactNormal       the normal of the plane that the point has hit
     * @param source              the calling entity
     */
    public RigidBody(PosVector hitPosition, DirVector contactNormal, Touchable source) {
        this.source = source;
        this.hitPosition = hitPosition;
        this.contactNormal = contactNormal;

        this.timeScalar = 1f;
        this.massCenterPosition = null;
        this.velocity = DirVector.zeroVector();
        this.rotationSpeedVector = new Vector3f();
        this.rotation = new Quaternionf();
        this.mass = Float.POSITIVE_INFINITY;
        invInertTensor = new Matrix3f();
    }

    /**
     * representation of a moving body for the sake of collision detection
     *
     * @param timeScalar         timeScalar of the relevant collision of the host object
     * @param massCenterPosition the global position of the center of mass at the moment of collision
     * @param velocity           the global speed vector of this object
     *                           This should contain the new velocity upon returning
     * @param hitPosition        global position of the point on this object that caused the collision
     * @param contactNormal      the normal of the plane that the point has hit. Must be normalized
     * @param rotation           current rotation of this object (unlikely to be relevant)
     * @param source             the calling entity
     * @param rollSpeed
     * @param pitchSpeed
     * @param yawSpeed
     */
    public RigidBody(float timeScalar, PosVector massCenterPosition, DirVector velocity, PosVector hitPosition,
                     DirVector contactNormal, Quaternionf rotation, float rollSpeed, float pitchSpeed, float yawSpeed, MovingEntity source) {
        this(timeScalar, massCenterPosition, velocity, hitPosition, contactNormal, new Vector3f(rollSpeed, pitchSpeed, yawSpeed), rotation, source);
    }

    /**
     * representation of a moving body for the sake of collision detection
     * @param timeScalar          timeScalar of the relevant collision of the host object
     * @param massCenterPosition  the global position of the center of mass at the moment of collision
     * @param velocity            the global speed vector of this object
     *                            This should contain the new velocity upon returning
     * @param hitPosition         global position of the point on this object that caused the collision
     * @param contactNormal       the normal of the plane that the point has hit. Must be normalized
     * @param rotationSpeedVector vector of rotation, defined as (rollSpeed, pitchSpeed, yawSpeed).
     *                            This should contain the new rotationspeed upon returning
     * @param rotation            current rotation of this object (unlikely to be relevant)
     * @param source              the calling entity
     */
    public RigidBody(float timeScalar, PosVector massCenterPosition, DirVector velocity, PosVector hitPosition,
                     DirVector contactNormal, Vector3f rotationSpeedVector, Quaternionf rotation, MovingEntity source) {
        this.timeScalar = timeScalar;
        this.massCenterPosition = massCenterPosition;
        this.velocity = velocity;
        this.hitPosition = hitPosition;
        this.contactNormal = contactNormal;
        this.rotationSpeedVector = rotationSpeedVector;
        this.rotation = rotation;
        this.source = source;
        this.mass = source.getMass();

        Matrix3f inertTensor = new Matrix3f().scale(mass);
        Matrix3f rotMatrix = rotation.get(new Matrix3f());
        Matrix3f rotInert = rotMatrix.mul(inertTensor, inertTensor);
        Matrix3f rotTranspose = rotMatrix.transpose();
        invInertTensor = rotInert.mul(rotTranspose).invert();
    }

    /**
     * representation of a static body that is not influenced in any way
     * @param source the calling entity
     */
    public RigidBody(Touchable source) {
        this.source = source;

        this.timeScalar = 1f;
        this.massCenterPosition = null;
        this.velocity = DirVector.zeroVector();
        this.hitPosition = null;
        this.contactNormal = null;
        this.rotationSpeedVector = new Vector3f();
        this.rotation = new Quaternionf();
        this.mass = Float.POSITIVE_INFINITY;
        invInertTensor = new Matrix3f();
    }

    /**
     * constructor for projectile objects
     * @param timeScalar          timeScalar of the relevant collision of the host object
     * @param globalHitPos        where this object hits the other in global space
     * @param velocity            the global speed vector of this entity
     * @param rotation            current rotation of this entity (unlikely to be relevant)
     * @param source              the calling entity
     */
    public RigidBody(float timeScalar, PosVector globalHitPos, DirVector velocity, Quaternionf rotation, MovingEntity source) {
        this.source = source;
        this.timeScalar = timeScalar;
        this.massCenterPosition = globalHitPos;
        this.velocity = velocity;
        this.hitPosition = massCenterPosition;
        this.contactNormal = DirVector.zeroVector();
        this.rotationSpeedVector = new Vector3f();
        this.rotation = rotation;
        this.mass = source.getMass();
        invInertTensor = new Matrix3f();
    }

    public void collisionWithStaticResponse() {
        collisionWithStaticResponseAlt(
                mass, invInertTensor, vectorToHitPos(),
                velocity, rotationSpeedVector, contactNormal
        );
    }

    private void collisionResponseSimple() {
        if (velocity.dot(contactNormal) < 0) velocity.reflect(contactNormal);
        Vector3f angularVelChange  = contactNormal.cross(hitPosition, new Vector3f());
        invInertTensor.transform(angularVelChange);
        rotationSpeedVector.add(angularVelChange);

        // prevent objects from extreme spinning
        rotationSpeedVector.mul(0.8f);
    }

    public float rollSpeed(){
        return rotationSpeedVector.x();
    }

    public float yawSpeed(){
        return rotationSpeedVector.z();
    }

    public float pitchSpeed(){
        return rotationSpeedVector.y();
    }

    /** False in most cases
     * @return true iff timeScalar == 1.
     */
    public boolean isFinal() {
        return timeScalar == 1f;
    }

    public DirVector vectorToHitPos(){
        return massCenterPosition.to(hitPosition, new DirVector());
    }

    /**
     * This function calculates the velocities after a 3D collision.
     * vaf, vbf, waf and wbf will contain the new velocities and rotations of the bodies
     * @param ma total mass of body a
     * @param mb total mass of body b
     * @param ra position of collision point relative to centre of mass of body a in absolute coordinates
     * @param rb position of collision point relative to centre of mass of body b in absolute coordinates
     * @param vai initial velocity of centre of mass on object a
     * @param vbi initial velocity of centre of mass on object b
     * @param wai initial angular velocity of object a
     * @param wbi initial angular velocity of object b
     * @param IIA inverse inertia tensor for body a in absolute coordinates
     * @param IIB inverse inertia tensor for body b in absolute coordinates
     * @param normal normal to collision point, the line along which the impulse acts according to body a. Must be normalized
     */
    private static void collisionResponseAlt(float ma, float mb, Vector3f ra, Vector3f rb, Vector3f vai, Vector3f vbi,
                                             Vector3f wai, Vector3f wbi, Matrix3f IIA, Matrix3f IIB, Vector3f normal) {

        final float upper = -1.5f * (vai.dot(normal));

        final Vector3f dOmegaA = new Vector3f(ra);
        dOmegaA.cross(normal);
        dOmegaA.mul(IIA);
        Vector3f rotationA = dOmegaA.cross(ra, new Vector3f());

        final Vector3f dOmegaB = new Vector3f(rb);
        dOmegaB.cross(normal);
        dOmegaB.mul(IIB);

        Vector3f rotationB = dOmegaB.cross(rb, new Vector3f());

        float rotFallOff = rotationA.add(rotationB, new Vector3f()).dot(normal);

        final float lower = (1 / mb) + (1 / ma) + rotFallOff;

        float jr = upper / lower;

        // vai = vai - ((Jr / ma) * normal)
        final Vector3f motionA = new Vector3f(normal);
        motionA.mul(jr / ma);
        vai.sub(motionA, vai);

        final Vector3f motionB = new Vector3f(normal);
        motionB.mul(jr / mb);
        vbi.add(motionB, vbi);

        wai.sub(dOmegaA.mul(jr), wai);
        wbi.add(dOmegaB.mul(jr), wbi);
    }

    /**
     * calculates the effect of colliding with an unmovable object
     * @param mass mass of this object
     * @param invertedInertiaTensor inverted inertia tensor of this object
     * @param hitPos relative position of collision in world-coordinates
     * @param velocity velocity of this object
     * @param rotationVec angular velocity of this object
     * @param normal normal to collision point, the line along which the impulse acts. Must be normalized.
     */
    private static void collisionWithStaticResponseAlt(float mass, Matrix3f invertedInertiaTensor, Vector hitPos, DirVector velocity, Vector3f rotationVec, DirVector normal){

        final float upper = -2f * (velocity.dot(normal));

        final Vector3f dOmega = new Vector3f(hitPos);
        dOmega.cross(normal);
        dOmega.mul(invertedInertiaTensor);

        Vector3f rotation = dOmega.cross(hitPos, new Vector3f());
        float rotImpulse = rotation.dot(normal);
        final float lower = (1f / mass) + rotImpulse;

        float jr = upper / lower;

        // vai = vai - ((Jr / ma) * normal)
        final Vector deltaVelocity = new DirVector(normal);
        deltaVelocity.mul(jr / mass);
        velocity.add(deltaVelocity, velocity);

        rotationVec.add(dOmega.mul(jr), rotationVec);
    }


    public void apply(float deltaTime, float currentTime) {
        if ((source instanceof MovingEntity)){
            ((MovingEntity) source).applyCollision(this, deltaTime, currentTime);
        }
    }

    /**
     * calculate response of collision between these objects
     *
     * @param alpha one entity, may be static
     * @param beta another object, may be static
     * @throws NullPointerException may be thrown if both bodies have infinite mass
     */
    @SuppressWarnings("ConstantConditions")
    public static void process(RigidBody alpha, RigidBody beta) {

        if (alpha.source instanceof AbstractProjectile) {
            final AbstractProjectile projectile = (AbstractProjectile) alpha.source;
            projectile.hit(beta.source, alpha.hitPosition);

        } else if (beta.source instanceof AbstractProjectile) {
            final AbstractProjectile projectile = (AbstractProjectile) beta.source;
            projectile.hit(alpha.source, beta.hitPosition);
        }

        // check for zero-mass collisions (which are ignored)
        if ((alpha.mass == 0) || (beta.mass == 0)) return;
        // check for projectile collisions; if there is no dynamic collision, this is ignored
        else if (COLLISION_RESPONSE_LEVEL < 2) {
            if ((alpha.source instanceof AbstractProjectile) || (beta.source instanceof AbstractProjectile)) return;
        }

        if (COLLISION_RESPONSE_LEVEL == 0) {
            if (alpha.contactNormal != null) alpha.collisionResponseSimple();
            if (beta.contactNormal != null) beta.collisionResponseSimple();

        } else if (COLLISION_RESPONSE_LEVEL == 1) {
            if (!Float.isInfinite(alpha.mass)) alpha.collisionWithStaticResponse();
            if (!Float.isInfinite(beta.mass)) beta.collisionWithStaticResponse();

        } else if (COLLISION_RESPONSE_LEVEL == 2) {
            if (Float.isInfinite(beta.mass)) alpha.collisionWithStaticResponse();
            else if (Float.isInfinite(alpha.mass)) beta.collisionWithStaticResponse();
            else dynamicCollisionResponse(alpha, beta);

        } else throw new IllegalArgumentException("COLLISION_RESPONSE_LEVEL = " + COLLISION_RESPONSE_LEVEL);
    }

    public static void dynamicCollisionResponse(RigidBody alpha, RigidBody beta) {
        collisionResponseAlt(
                alpha.mass, beta.mass,
                alpha.vectorToHitPos(), beta.vectorToHitPos(),
                alpha.velocity, beta.velocity,
                alpha.rotationSpeedVector, beta.rotationSpeedVector,
                alpha.invInertTensor, beta.invInertTensor,
                alpha.contactNormal
        );
    }
}
