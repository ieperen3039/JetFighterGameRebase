package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen. Created on 21-7-2018.
 */
public class RocketAI extends AI {
    private static final float DIRECTION_THROTTLE_MODIFIER = 1f;
    private static final float DIRECTION_ROLL_MODIFIER = 2f;
    private static final float DIRECTION_PITCH_MODIFIER = 2f;
    private static final float DIRECTION_YAW_MODIFIER = 1f;

    private final MovingEntity projectile;

    private MovingEntity target;
    private final float pSpeedSq;
    private PosVector targetPos = new PosVector();

    private DirVector vecToTarget;
    private DirVector yVec;
    private DirVector zVec;

    /**
     * a controller that tries to send the projectile in the anticipated direction of target, assuming the given speed
     * @param targetTps       the number of times this AI should be updated
     * @param projectile      the projectile that is controlled by this controller
     * @param target          the target entity that this projectile tries to hit
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given projectile
     */
    public RocketAI(int targetTps, MovingEntity projectile, MovingEntity target, float projectileSpeed) {
        super("RocketAI(" + projectile + ")", targetTps);
        this.projectile = projectile;
        this.target = target;
        this.pSpeedSq = projectileSpeed * projectileSpeed;
        update(0f);
        update();
    }

    public void setTarget(MovingEntity target) {
        this.target = target;
        update(0f);
        update();
    }

    @Override
    public void update() {
        vecToTarget = projectile.getPosition().to(targetPos, new DirVector());
        yVec = projectile.relativeStateDirection(DirVector.yVector());
        zVec = projectile.relativeStateDirection(DirVector.zVector());
    }

    @Override
    public float throttle() {
        float dot = projectile.getVelocity().dot(vecToTarget);
        return Math.min(1, Math.max(0, dot * DIRECTION_THROTTLE_MODIFIER));
    }

    @Override
    public float pitch() {
        float dot = zVec.dot(vecToTarget);
        return Math.min(1, Math.max(0, dot * DIRECTION_PITCH_MODIFIER));
    }

    @Override
    public float yaw() {
        float dot = yVec.dot(vecToTarget);
        return Math.min(1, Math.max(0, dot * DIRECTION_YAW_MODIFIER));
    }

    @Override
    public float roll() {
        float dot = projectile.getVelocity().dot(vecToTarget);
        float mod = zVec.dot(vecToTarget) * (1 - dot);
        return Math.min(1, Math.max(-1, mod * DIRECTION_ROLL_MODIFIER));
    }

    @Override
    public boolean primaryFire() {
        return false;
    }

    @Override
    public boolean secondaryFire() {
        return false;
    }

    @Override
    protected void update(float deltaTime) {
        DirVector tVel = target.getVelocity();
        PosVector tPos = target.getPosition();
        Vector3f relPos = tPos.sub(projectile.getPosition());

        float a = tVel.lengthSquared();
        float b = tVel.dot(relPos);
        float c = relPos.lengthSquared() - pSpeedSq;
        float d = (b * b) - (4 * a * c);

        float lambda = (-b + (float) Math.sqrt(d)) / (2 * a);

        tPos.add(tVel.scale(lambda, new DirVector()), targetPos);
    }

    @Override
    protected void cleanup() {

    }
}
