package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTracked;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    /** camera settings */
    private static final DirVector eyeRelative = new DirVector(-7, 0, 2);
    private static final DirVector focusRelativeToEye = new DirVector(5, 0, 0);
    public static final float CAMERA_CATCHUP = 0.9999f; // speed of camera positioning
    private static final float CAMERA_ORIENT = 0.9999f; // linear speed of camera orientation

    /**
     * The position of the camera.
     */
    private final SmoothTracked<PosVector> eye;
    private final SmoothTracked<DirVector> focus;
    private final SmoothTracked<DirVector> up;
    private final GameEntity target;

    public FollowingCamera(GameEntity target) {
        this(jetPosition(eyeRelative, target).toPosVector(), target, jetPosition(DirVector.zVector(), target).toDirVector());
    }

    public FollowingCamera(PosVector eye, GameEntity playerJet, DirVector up) {
        this(
                new ExponentialSmoothVector<>(eye, 1- CAMERA_CATCHUP),
                new ExponentialSmoothVector<>(jetPosition(focusRelativeToEye, playerJet).toDirVector(), 1- CAMERA_ORIENT),
                new ExponentialSmoothVector<>(up, 1-CAMERA_ORIENT),
                playerJet
        );
    }

    public FollowingCamera(SmoothTracked<PosVector> eye, SmoothTracked<DirVector> focus, SmoothTracked<DirVector> up, GameEntity target) {
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
    private static PosVector jetPosition(DirVector relativePosition, GameEntity target){
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

        if ((target instanceof MortalEntity) && ((MortalEntity) target).isDead()){
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
