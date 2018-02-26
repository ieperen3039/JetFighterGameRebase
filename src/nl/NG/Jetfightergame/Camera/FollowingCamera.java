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
    private static final DirVector eyeRelative = new DirVector(-10, 0, 4);
    private static final DirVector focusRelative = eyeRelative.add(new DirVector(20, 0, 0), new DirVector());
    public static final float CAMERA_CATCHUP = 0.95f; // speed of camera positioning
    private static final float CAMERA_ORIENT = 0.99f; // speed of camera orientation

    /**
     * The position of the camera.
     */
    private final SmoothTracked<PosVector> eye;
    private final SmoothTracked<PosVector> focus;
    private final SmoothTracked<DirVector> up;
    private final GameEntity target;

    public FollowingCamera(GameEntity target) {
        this(jetPosition(eyeRelative, target).toPosVector(), target, jetPosition(DirVector.zVector(), target).toDirVector());
    }

    public FollowingCamera(PosVector eye, GameEntity playerJet, DirVector up) {
        this(
                new ExponentialSmoothVector<>(eye, 1- CAMERA_CATCHUP),
                new ExponentialSmoothVector<>(jetPosition(focusRelative, playerJet).toPosVector(), 1- CAMERA_ORIENT),
                new ExponentialSmoothVector<>(up, 1- CAMERA_ORIENT),
                playerJet
        );
    }

    public FollowingCamera(SmoothTracked<PosVector> eye, SmoothTracked<PosVector> focus, SmoothTracked<DirVector> up, GameEntity target) {
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
        if ((target instanceof MortalEntity) && ((MortalEntity) target).isDead()){
            return target.getPosition();
        }
        final DirVector relative = target.relativeInterpolatedDirection(relativePosition);
        return target.interpolatedPosition().add(relative, new PosVector());
    }

    /**
     * @param deltaTime the animation time difference
     */
    @Override
    public void updatePosition(float deltaTime) {
        final PosVector targetPosition = target.interpolatedPosition();

        final DirVector up = target.relativeInterpolatedDirection(DirVector.zVector());
        final DirVector targetUp = up.normalize(up);

        final PosVector targetEye = targetPosition.add(target.relativeInterpolatedDirection(eyeRelative), new PosVector());
        final PosVector targetFocus = targetPosition.add(target.relativeInterpolatedDirection(focusRelative), new PosVector());

        eye.updateFluent(targetEye, deltaTime);
        focus.updateFluent(targetFocus, deltaTime);
        this.up.updateFluent(targetUp, deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return getEye().to(getFocus(), new DirVector());
    }

    @Override
    public PosVector getEye() {
        return eye.current();
    }

    @Override
    public PosVector getFocus() {
        return focus.current();
    }

    @Override
    public DirVector getUpVector() {
        return up.current();
    }
}
