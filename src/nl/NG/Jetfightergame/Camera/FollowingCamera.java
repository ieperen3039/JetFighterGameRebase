package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    /** camera settings */
    private static final DirVector eyeRelative = new DirVector(-20, 0, 3);
    private static final DirVector focusRelativeToEye = new DirVector(5, 0, 0);
    private static final float CAMERA_CATCHUP = 0.99999f; // speed of camera positioning
    private static final float CAMERA_ORIENT = 0.9999f; // linear speed of camera orientation

    /**
     * The position of the camera.
     */
    private final SmoothTrackedVector<PosVector> eye;
    private final SmoothTrackedVector<DirVector> focus;
    private final SmoothTrackedVector<DirVector> up;
    private final MovingEntity target;

    public FollowingCamera(MovingEntity target) {
        this(jetPosition(eyeRelative, target).toPosVector(), target, jetPosition(DirVector.zVector(), target).toDirVector());
    }

    public FollowingCamera(PosVector eye, MovingEntity playerJet, DirVector up) {
        this(
                new ExponentialSmoothVector<>(eye, 1- CAMERA_CATCHUP),
                new ExponentialSmoothVector<>(jetPosition(focusRelativeToEye, playerJet).toDirVector(), 1- CAMERA_ORIENT),
                new ExponentialSmoothVector<>(up, 1-CAMERA_ORIENT),
                playerJet
        );
    }

    public FollowingCamera(SmoothTrackedVector<PosVector> eye, SmoothTrackedVector<DirVector> focus, SmoothTrackedVector<DirVector> up, MovingEntity target) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
        this.target = target;
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
            targetFocus = new DirVector(eye.current());
            targetFocus.negate();
        } else {
            targetFocus = target.relativeInterpolatedDirection(DirVector.xVector());
        }

        eye.updateFluent(targetEye, deltaTime);
        focus.updateFluent(targetFocus, deltaTime);
        up.updateFluent(targetUp, deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return focus.current();
    }

    @Override
    public PosVector getEye() {
        return eye.current();
    }

    @Override
    public PosVector getFocus() {
        return eye.current().add(focus.current(), new PosVector());
    }

    @Override
    public DirVector getUpVector() {
        return up.current();
    }
}
