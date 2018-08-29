package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen. Created on 21-7-2018.
 */
public class RocketAI implements Controller {
    private final float rollFactor;
    private final float pitchFactor;
    private final float yawFactor;
    private static final float THROTTLE_DOT_IGNORE = (float) Math.cos(0.5f);
    private static final float THROTTLE_MULTIPLIER = 1 / (1 - THROTTLE_DOT_IGNORE);

    public final MovingEntity projectile;

    protected Touchable target;
    private final float pSpeed;
    private PosVector projectilePos;
    protected PosVector targetPos;

    protected DirVector vecToTarget;
    protected DirVector xVec;
    protected DirVector yVec;
    protected DirVector zVec;
    protected float explodeDistSq;
    private final boolean doExtrapolate;

    /**
     * a controller that tries to send the projectile in the anticipated direction of target, assuming the given speed
     * @param projectile      the projectile that is controlled by this controller
     * @param target          the target entity that this projectile tries to hit
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given projectile
     * @param explodeDistance only if the controlled entity is within a range of explodeDistance, primaryFire() will
     *                        return true
     */
    public RocketAI(MovingEntity projectile, Touchable target, float projectileSpeed, float explodeDistance) {
        this.projectile = projectile;
        this.target = target;
        this.pSpeed = projectileSpeed;
        this.explodeDistSq = explodeDistance * explodeDistance;
        doExtrapolate = true;
        projectilePos = projectile.getPosition();
        targetPos = target.getExpectedMiddle();
        rollFactor = 0.1f;
        pitchFactor = 2.5f;
        yawFactor = 2.5f;
    }

    /**
     * a controller that sends the given projectile to the given target
     * @param projectile the projectile that is controlled by this controller
     * @param target     the target entity that this projectile tries to hit
     * @param fireDist   only if the controlled entity is within a range of fireDist, primaryFire() will return true
     * @param rollFac    the roll output is multiplied with this factor
     * @param pitchFac   the pitch output is multiplied with this factor
     * @param yawFac     the yaw output is multiplied with this factor
     */
    public RocketAI(MovingEntity projectile, Touchable target, float fireDist, float rollFac, float pitchFac, float yawFac) {
        this.projectile = projectile;
        this.target = target;
        this.pSpeed = 0;
        this.explodeDistSq = fireDist * fireDist;
        doExtrapolate = false;
        projectilePos = projectile.getPosition();
        rollFactor = rollFac;
        pitchFactor = pitchFac;
        yawFactor = yawFac;
    }

    @Override
    public void update() {
        if (TemporalEntity.isOverdue(target)) return;

        projectilePos = projectile.getPosition();
        targetPos = getTargetPosition();

        vecToTarget = projectilePos.to(targetPos, new DirVector());
        vecToTarget.normalize();
        xVec = projectile.relativeDirection(DirVector.xVector());
        yVec = projectile.relativeDirection(DirVector.yVector());
        zVec = projectile.relativeDirection(DirVector.zVector());

        xVec.normalize();
        yVec.normalize();
        zVec.normalize();
    }

    protected PosVector getTargetPosition() {
        if (target == null) {
            return new PosVector();
        }

        PosVector tPos = target.getExpectedMiddle();
        if (doExtrapolate && target instanceof MovingEntity && !arrivesWithin(tPos, 0.2f)) {
            MovingEntity entity = (MovingEntity) this.target;

            return extrapolateTarget(entity.getVelocity(), tPos, projectilePos, pSpeed);
        }

        return tPos;
    }

    private boolean arrivesWithin(PosVector tPos, float time) {
        return projectilePos.distanceSquared(tPos) < (time * pSpeed * pSpeed);
    }

    /**
     * @param tVel   velocity of the target
     * @param tPos   current position of the target
     * @param sPos   current position of the source
     * @param sSpeed the estimated speed of source
     * @return position S such that if a position on sPos would move with a speed of sSpeed toward S, he would meet the
     *         target at S
     */
    public static PosVector extrapolateTarget(DirVector tVel, PosVector tPos, PosVector sPos, float sSpeed) {
        Vector3f relPos = sPos.to(tPos, new DirVector());

        // || xA + B || = v
        // with A is the target velocity and B the relative position of the target to the projectile
        // solving for x gives a quadratic function, which can be solved using the ABC formula
        float a = tVel.lengthSquared() - sSpeed * sSpeed;
        float b = 2 * tVel.dot(relPos);
        float c = relPos.lengthSquared();
        float d = (b * b) - (4 * a * c);

        if (d > 0) {
            // one of these solutions is negative
            float lambda1 = (-b + (float) Math.sqrt(d)) / (2 * a);
            float lambda2 = (-b - (float) Math.sqrt(d)) / (2 * a);
            float exFac = Math.max(lambda1, lambda2);
            tPos.add(tVel.scale(exFac), tPos);
        }

        return tPos;
    }

    @Override
    public float throttle() {
        if (targetPos != null && arrivesWithin(targetPos, 1f)) {
            return 1;
        }

        float dot = xVec.dot(vecToTarget);
        dot -= THROTTLE_DOT_IGNORE;
        return bound(dot * THROTTLE_MULTIPLIER, 0, 1);
    }

    @Override
    public float pitch() {
        float dot = zVec.dot(vecToTarget);
        return bound(dot * pitchFactor, -1, 1);
    }

    @Override
    public float yaw() {
        float dot = -yVec.dot(vecToTarget);
        return bound(dot * yawFactor, -1, 1);
    }

    @Override
    public float roll() {
        DirVector cross = vecToTarget.cross(xVec, new DirVector());
        float dot = zVec.dot(cross);
        return bound(dot * rollFactor, -1, 1);
    }


    public static float bound(float input, float lower, float upper) {
        return (input < lower) ? lower : ((input > upper) ? upper : input);
    }

    @Override
    public boolean primaryFire() {
        return targetPos != null && projectilePos.distanceSquared(targetPos) < explodeDistSq;
    }

    @Override
    public boolean secondaryFire() {
        return false;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }

}
