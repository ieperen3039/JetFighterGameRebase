package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    private Matrix3f inertTensor;

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
        inertTensor = new Matrix3f();
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
        this.velocity = new DirVector(velocity);
        this.hitPosition = hitPosition;
        this.contactNormal = contactNormal;
        this.rotationSpeedVector = rotationSpeedVector;
        this.rotation = rotation;
        this.source = source;
        this.mass = source.getMass();
        inertTensor = new Matrix3f().scale(mass);
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
        inertTensor = new Matrix3f();
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
     * @param Ia inertia tensor for body a in absolute coordinates
     * @param Ib inertia tensor for body b in absolute coordinates
     * @param ra position of collision point relative to centre of mass of body a in absolute coordinates
     * @param rb position of collision point relative to centre of mass of body b in absolute coordinates
     * @param vai initial velocity of centre of mass on object a
     * @param vbi initial velocity of centre of mass on object b
     * @param wai initial angular velocity of object a
     * @param wbi initial angular velocity of object b
     * @param normal normal to collision point, the line along which the impulse acts. Must be normalized.
     *
     * @author 1998-2017 Richard, Steve or Todd
     */
    private static void collisionResponse(float ma, float mb, Matrix3f Ia, Matrix3f Ib, Vector3f ra, Vector3f rb, Vector3f vai,
                                          Vector3f vbi, Vector3f wai, Vector3f wbi, Vector3f normal) {

        Vector3f angularVelChangea  = normal.cross(ra, new Vector3f()); // start calculating the change in angular rotation of a
        Ia.invert().transform(angularVelChangea);
        Vector3f vaLinDueToR = angularVelChangea.cross(ra, new Vector3f());  // calculate the linear velocity of collision point on a due to rotation of a
        float scalar = (1 / ma) + vaLinDueToR.dot(normal);

        normal.negate();

        Vector3f angularVelChangeb = normal.cross(rb, new Vector3f());
        Ib.invert().transform(angularVelChangeb);
        Vector3f vbLinDueToR = angularVelChangeb.cross(rb, new Vector3f());  // calculate the linear velocity of collision point on b due to rotation of b
        scalar += (1 / mb) + vbLinDueToR.dot(normal);

        final float netSpeed = vai.sub(vbi, new Vector3f()).length();
        float Jmod = 2 * (netSpeed / scalar);
        Vector3f J = normal.mul(Jmod, new Vector3f());

        vai.sub(J.mul(1 / ma), vai);
        vbi.sub(J.mul(1 / mb), vbi);
        wai.sub(angularVelChangea, wai);
        wbi.sub(angularVelChangeb, wbi);
    }

    /**
     * calculates the effect of colliding with an unmovable object
     * @param mass mass of this object
     * @param InertiaTensor inertia tensor of this object
     * @param hitPos relative position of collision in world-coordinates
     * @param velocity velocity of this object
     * @param rotationVec angular velocity of this object
     * @param normal normal to collision point, the line along which the impulse acts. Must be normalized.
     */
    private static void collisionWithStaticResponse(float mass, Matrix3f InertiaTensor, Vector hitPos, DirVector velocity, Vector3f rotationVec, DirVector normal){
        Vector3f angularVelChange  = normal.cross(hitPos, new Vector3f()); // start calculating the change in angular rotation of a
        InertiaTensor.invert().transform(angularVelChange);
        Vector3f velocityByRotation = angularVelChange.cross(hitPos, new Vector3f());  // calculate the linear velocity of collision point on a due to rotation of a
        float scalar = (1 / mass) + velocityByRotation.dot(normal);

        final float netSpeed = velocity.length();
        float Jmod = 2 * (netSpeed / scalar);
        Vector3f J = normal.mul(Jmod, new Vector3f());

        velocity.sub(J.mul(1 / mass), velocity);
        rotationVec.sub(angularVelChange, rotationVec);
    }

    private static void collisionResponseSimple(RigidBody alpha, RigidBody beta){
        if (alpha.contactNormal != null) alpha.velocity.reflect(alpha.contactNormal);
        if (beta.contactNormal != null) beta.velocity.reflect(beta.contactNormal);
    }

    public void apply(float deltaTime, float currentTime) {
        if ((source instanceof MovingEntity) && !Float.isInfinite(mass)){
            ((MovingEntity) source).applyCollision(this, deltaTime, currentTime);
        }
    }

    /**
     * calculate response of collision between these objects
     *
     * @param alpha one entity, may be static
     * @param beta another object, may be static
     * @throws NullPointerException if both bodies are of infinite mass
     */
    public static void process(RigidBody alpha, RigidBody beta) {

        if (Settings.SIMPLE_COLLISION_RESPONSE){
            collisionResponseSimple(alpha, beta);
        } else {

            RigidBody body = null;
            if (Float.isInfinite(alpha.mass)) body = beta;
            else if (Float.isInfinite(beta.mass)) body = alpha;

            if (body == null) {
                collisionResponse(
                        alpha.mass, beta.mass,
                        alpha.inertTensor, beta.inertTensor,
                        alpha.vectorToHitPos(), beta.vectorToHitPos(),
                        alpha.velocity, beta.velocity,
                        alpha.rotationSpeedVector, beta.rotationSpeedVector,
                        alpha.contactNormal
                );
            } else {
                collisionWithStaticResponse(
                        body.mass, body.inertTensor, body.vectorToHitPos(),
                        body.velocity, body.rotationSpeedVector, body.contactNormal
                );
            }
        }
    }
}
