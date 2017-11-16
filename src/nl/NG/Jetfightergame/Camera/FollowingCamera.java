package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.Engine.GLMatrix.AxisBasedGL;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    private static final DirVector relativePosition = new DirVector(-5, 0, 3);
    private static final DirVector relativeLookAt = relativePosition.add(new DirVector(10, 0, -1));
    public static final float CAMERA_CATCHUP = 0.8f;
    /**
     * The position of the camera.
     */
    private final ExponentialSmoothVector<PosVector> eye;
    /**
     * The up vector.
     */
    private final DirVector up;
    private final AbstractJet target;

    public FollowingCamera(AbstractJet target) {
        this(target.getPosition(), target);
    }

    public FollowingCamera(PosVector eye, AbstractJet playerJet) {
        this(new ExponentialSmoothVector<>(eye, 1-CAMERA_CATCHUP), DirVector.Z, playerJet);
    }

    public FollowingCamera(ExponentialSmoothVector<PosVector> eye, DirVector up, AbstractJet target) {
        this.eye = eye;
        this.up = up;
        this.target = target;
    }

    /**
     * @param relativePosition a position relative to target
     * @param target a target jet, where DirVector.X points forward
     * @return the position translated to world-space
     */
    private static PosVector getRelative(DirVector relativePosition, AbstractJet target){
        AxisBasedGL ws = new AxisBasedGL(gl);
        return target.getPosition().add(target.getRelativeVector(relativePosition, ws));
    }

    /**
     * @param deltaTime the real time difference (not animation time difference)
     */
    @Override
    public void updatePosition(float deltaTime) {
        eye.updateFluent(getRelative(relativePosition, target), deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return eye.current().to(target.getPosition());
    }

    @Override
    public PosVector getEye() {
        return eye.current();
    }

    @Override
    public PosVector getFocus() {
        return getRelative(relativeLookAt, target);
    }

    @Override
    public DirVector getUpVector() {
        return up;
    }
}
