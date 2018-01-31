package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 24-1-2018.
 */
public abstract class AbstractProjectile extends GameEntity implements MortalEntity {

    private DirVector forward;
    private final float airResistCoeff;

    protected float timeToLive;

    public AbstractProjectile(
            Material surfaceMaterial, float mass, float scale, PosVector initialPosition, DirVector initialVelocity,
            Quaternionf initialRotation, GameTimer gameTimer, float airResistCoeff, float timeToLive
    ) {
        super(surfaceMaterial, mass, scale, initialPosition, initialVelocity, initialRotation, gameTimer);
        this.airResistCoeff = airResistCoeff;
        this.timeToLive = timeToLive;

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {

        netForce.add(forward.reducedTo(getThrust(forward), new DirVector()), netForce);

        DirVector airResistance = new DirVector();
        float speed = velocity.length();
        velocity.reducedTo(speed * speed * airResistCoeff * -1, airResistance);
        velocity.add(airResistance.scale(deltaTime, airResistance));
        relativeStateDirection(DirVector.xVector()).normalize(forward);

        adjustOrientation(extraPosition, extraRotation, extraVelocity, forward, deltaTime);

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);

        timeToLive -= deltaTime;
    }

    @Override
    public RigidBody getFinalCollision(float deltaTime) {
        // global position of contact
        final PosVector globalHitPos = nextCrash.get().hitPos;
        // fraction of deltaTime until collision
        final float timeScalar = nextCrash.get().timeScalar * 0.99f; // 0.99f to prevent rounding errors.

        return new RigidBody(
//                timeScalar, globalHitPos, extraVelocity, rotation.nlerp(extraRotation, timeScalar), this
                timeScalar, globalHitPos, extraVelocity, globalHitPos, nextCrash.get().normal, extraRotation, rollSpeed, pitchSpeed, yawSpeed, this
        );
    }

    @Override
    public void applyCollision(RigidBody newState, float deltaTime, float currentTime) {
        // add final state before this projectile is gone
        positionInterpolator.add(newState.hitPosition, currentTime);
        rotationInterpolator.add(newState.rotation, currentTime);

        timeToLive = 0;
    }

    /**
     * react on projectile collision impact, or simply explode if {@code other} is null
     * @param other collidee; the one that is collided upon; the thing that this thing hit
     * @param hitPosition exact world-position of collision
     */
    public abstract void hit(Touchable other, PosVector hitPosition);

    @Override
    public void impact(PosVector impact, float power) {
        // reward "Crimea War" achievement (two projectiles collide)
    }

    @Override
    public boolean isDead() {
        return timeToLive <= 0;
    }

    /**
     * update state of this projectile according to input. This method is called after the physics are applied
     * @param extraPosition the place to store the new position
     * @param extraRotation the place to store the new rotation
     * @param extraVelocity the place to store the new velocity
     * @param forward vector pointing along the x-axis of the plane in world-space
     * @param deltaTime time difference
     */
    protected abstract void adjustOrientation(
            PosVector extraPosition, Quaternionf extraRotation, DirVector extraVelocity, DirVector forward, float deltaTime);

    protected abstract float getThrust(DirVector forward);
}
