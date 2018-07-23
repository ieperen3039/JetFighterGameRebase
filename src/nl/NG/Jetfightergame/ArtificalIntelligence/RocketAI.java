package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen. Created on 21-7-2018.
 */
public class RocketAI implements Controller {
    private static final float DIRECTION_THROTTLE_MODIFIER = 0.5f;
    private static final float DIRECTION_ROLL_MODIFIER = 2.0f;
    private static final float DIRECTION_PITCH_MODIFIER = 1.5f;
    private static final float DIRECTION_YAW_MODIFIER = 0.5f;
    private static final float BLOW_DIST_SQ = 100;
    public static final float THROTTLE_DOT_IGNORE = 0.3f;
    public static final float SHOOT_ACCURACY = 0.01f;

    private final MovingEntity projectile;

    private MovingEntity target;
    private final float pSpeedSq;
    private PosVector targetPos = new PosVector();

    private DirVector vecToTarget;
    private DirVector xVec;
    private DirVector yVec;
    private DirVector zVec;
    private boolean doAim;

    /**
     * a controller that tries to send the projectile in the anticipated direction of target, assuming the given speed
     * @param projectile      the projectile that is controlled by this controller
     * @param target          the target entity that this projectile tries to hit
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given projectile
     * @param doAim
     */
    public RocketAI(MovingEntity projectile, MovingEntity target, float projectileSpeed, boolean doAim) {
        this.projectile = projectile;
        this.target = target;
        this.pSpeedSq = projectileSpeed * projectileSpeed;
        this.doAim = doAim;
        update();
    }

    public void setTarget(MovingEntity target) {
        this.target = target;
        update();
    }

    @Override
    public void update() {
        DirVector tVel = target.getVelocity();
        PosVector tPos = target.getPosition();
        PosVector prPos = projectile.getPosition();
        Vector3f relPos = prPos.to(tPos, new DirVector());

        float a = tVel.lengthSquared() - pSpeedSq;
        float b = 2 * tVel.dot(relPos);
        float c = relPos.lengthSquared();
        float d = (b * b) - (4 * a * c);

        if (d > 0) {
            float lambda1 = (-b + (float) Math.sqrt(d)) / (2 * a);
            float lambda2 = (-b - (float) Math.sqrt(d)) / (2 * a);
            float exFac = Math.max(lambda1, lambda2);
            tPos.add(tVel.scale(exFac, new DirVector()), targetPos); // S

        } else {
            targetPos = tPos;
        }

        vecToTarget = prPos.to(targetPos, new DirVector());
        vecToTarget.normalize();
        xVec = projectile.relativeStateDirection(DirVector.xVector());
        yVec = projectile.relativeStateDirection(DirVector.yVector());
        zVec = projectile.relativeStateDirection(DirVector.zVector());

        xVec.normalize();
        yVec.normalize();
        zVec.normalize();
    }

    @Override
    public float throttle() {
        float dot = xVec.dot(vecToTarget);
        dot -= THROTTLE_DOT_IGNORE;
        return Math.min(1, Math.max(0, dot * DIRECTION_THROTTLE_MODIFIER));
    }

    @Override
    public float pitch() {
        float dot = zVec.dot(vecToTarget);
        return Math.min(1, Math.max(-1, -dot * DIRECTION_PITCH_MODIFIER));
    }

    @Override
    public float yaw() {
        float dot = yVec.dot(vecToTarget);
        return Math.min(1, Math.max(-1, dot * DIRECTION_YAW_MODIFIER));
    }

    @Override
    public float roll() {
        DirVector cross = vecToTarget.cross(xVec, new DirVector());
        float dot = zVec.dot(cross);
        return Math.min(1, Math.max(-1, dot * DIRECTION_ROLL_MODIFIER));
    }

    @Override
    public boolean primaryFire() {
        if (doAim) {
            return xVec.dot(vecToTarget) > (1 - SHOOT_ACCURACY);
        } else {
            return projectile.getPosition().sub(targetPos).lengthSquared() < BLOW_DIST_SQ;
        }
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
