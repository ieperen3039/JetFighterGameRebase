package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    /** camera settings */
    private static final DirVector eyeRelative = new DirVector(-20, 0, 5);
    private static final double EYE_PRESERVE = 1.0E-5;
    private static final double ORIENT_PRESERVE = 1.0E-4;
    private static final float TELEPORT_DISTANCE_SQ = 50f * 50f;

    /**
     * The position of the camera.
     */
    private final SmoothTrackedVector<PosVector> eye;
    private final SmoothTrackedVector<DirVector> vecToFocus;
    private final SmoothTrackedVector<DirVector> up;
    private final Player target;
    private final Environment gameState;
    private DirVector velocity = new DirVector();

    public FollowingCamera(Player target, Environment gameState) {
        this(jetPosition(eyeRelative,
                target.jet()).toPosVector(), target,
                jetPosition(DirVector.zVector(), target.jet()).toDirVector(),
                target.jet().getVelocity(), gameState
        );
    }

    public FollowingCamera(PosVector eye, Player playerJet, DirVector up, DirVector vecToFocus, Environment gameState) {
        this.eye = new ExponentialSmoothVector<>(eye, EYE_PRESERVE);
        this.vecToFocus = new ExponentialSmoothVector<>(vecToFocus, ORIENT_PRESERVE);
        this.up = new ExponentialSmoothVector<>(up, ORIENT_PRESERVE);
        this.target = playerJet;
        this.gameState = gameState;
    }

    /**
     * @param relativePosition a position relative to target
     * @param target a target jet, where DirVector.X points forward
     * @return a new vector with the position translated to world-space
     */
    private static PosVector jetPosition(DirVector relativePosition, MovingEntity target) {
        PosVector targetPos = target.getPosition();
        if (!targetPos.isScalable()) return PosVector.zeroVector();

        final DirVector relative = target.relativeDirection(relativePosition);
        return targetPos.add(relative, new PosVector());
    }

    /**
     * @param deltaTime the animation time difference
     */
    @Override
    public void updatePosition(float deltaTime) {
        MovingEntity target = this.target.jet();
        final DirVector rawUp = target.relativeDirection(DirVector.zVector());
        final DirVector targetUp = rawUp.normalize(rawUp);

        final PosVector targetEye = jetPosition(eyeRelative, target);
        final DirVector targetFocus;

        if (TemporalEntity.isOverdue(target)) {
            targetFocus = new DirVector(eye.current()); // look at (0, 0, 0)
            targetFocus.negate();
        } else {
            targetFocus = target.relativeDirection(DirVector.xVector());
        }

        if (eye.current().distanceSquared(targetEye) > TELEPORT_DISTANCE_SQ) eye.update(targetEye);
        else eye.updateFluent(targetEye, deltaTime);
        vecToFocus.updateFluent(targetFocus, deltaTime);
        up.updateFluent(targetUp, deltaTime);
        velocity = eye.difference();
        if (deltaTime != 0) velocity.scale(1 / deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return vecToFocus.current();
    }

    @Override
    public PosVector getEye() {
        PosVector pos = target.jet().getPosition();
        return gameState.rayTrace(pos, eye.current());
    }

    @Override
    public PosVector getFocus() {
        return getEye().add(vectorToFocus(), new PosVector());
    }

    @Override
    public DirVector getUpVector() {
        return up.current();
    }

    @Override
    public DirVector getVelocity() {
        return velocity;
    }
}
