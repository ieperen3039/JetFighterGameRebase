package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    /** camera settings */
    private static final DirVector eyeRelative = new DirVector(-200, 0, 10);
    private static final double EYE_PRESERVE = 1.0E-5;
    private static final double ORIENT_PRESERVE = 1.0E-4;

    /**
     * The position of the camera.
     */
    private final SmoothTrackedVector<PosVector> eye;
    private final SmoothTrackedVector<DirVector> vecToFocus;
    private final SmoothTrackedVector<DirVector> up;
    private final MovingEntity target;
    private final Environment gameState;

    public FollowingCamera(MovingEntity target, Environment gameState) {
        this(jetPosition(eyeRelative, target).toPosVector(), target, jetPosition(DirVector.zVector(), target).toDirVector(), target.getVelocity(), gameState);
    }

    public FollowingCamera(PosVector eye, MovingEntity playerJet, DirVector up, DirVector vecToFocus, Environment gameState) {
        this(
                new ExponentialSmoothVector<>(eye, EYE_PRESERVE),
                new ExponentialSmoothVector<>(vecToFocus, ORIENT_PRESERVE),
                new ExponentialSmoothVector<>(up, ORIENT_PRESERVE),
                playerJet, gameState
        );
    }

    public FollowingCamera(
            SmoothTrackedVector<PosVector> eye,
            SmoothTrackedVector<DirVector> vecToFocus,
            SmoothTrackedVector<DirVector> up,
            MovingEntity target, Environment gameState
    ) {
        this.eye = eye;
        this.vecToFocus = vecToFocus;
        this.up = up;
        this.target = target;
        this.gameState = gameState;
    }

    /**
     * @param relativePosition a position relative to target
     * @param target a target jet, where DirVector.X points forward
     * @return a new vector with the position translated to world-space
     */
    private static PosVector jetPosition(DirVector relativePosition, MovingEntity target) {
        final DirVector relative = target.relativeInterpolatedDirection(relativePosition);
        return target.interpolatedPosition().add(relative, new PosVector());
    }

    /**
     * @param deltaTime the animation time difference
     */
    @Override
    public void updatePosition(float deltaTime) {
        final DirVector rawUp = target.relativeInterpolatedDirection(DirVector.zVector());
        final DirVector targetUp = rawUp.normalize(rawUp);

        final PosVector targetEye = jetPosition(eyeRelative, target);
        final DirVector targetFocus;

        if (TemporalEntity.isOverdue(target)) {
            targetFocus = new DirVector(eye.current()); // look at (0, 0, 0)
            targetFocus.negate();
        } else {
            targetFocus = target.relativeInterpolatedDirection(DirVector.xVector());
        }

        eye.updateFluent(targetEye, deltaTime);
        vecToFocus.updateFluent(targetFocus, deltaTime);
        up.updateFluent(targetUp, deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return vecToFocus.current();
    }

    @Override
    public PosVector getEye() {
        PosVector pos = target.interpolatedPosition();
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
}
