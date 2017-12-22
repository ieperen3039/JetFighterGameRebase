package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.EntityDefinitions.GameEntity;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    private static final DirVector relativePosition = new DirVector(-10, 0, 4);
    private static final DirVector relativeLookAt = relativePosition.add(new DirVector(10, 0, -1), new DirVector());
    private static final float CAMERA_ORIENT = 0.9f; // speed of camera orientation
    /**
     * The position of the camera.
     */
    private final ExponentialSmoothVector<PosVector> eye;
    private final ExponentialSmoothVector<PosVector> focus;
    private final ExponentialSmoothVector<DirVector> up;
    private final GameEntity target;

    public FollowingCamera(GameEntity target) {
        this(jetPosition(relativePosition, target).toPosVector(), target);
    }

    public FollowingCamera(PosVector eye, GameEntity playerJet) {
        this(
                new ExponentialSmoothVector<>(eye, 1- Settings.CAMERA_CATCHUP),
                new ExponentialSmoothVector<>(jetPosition(relativeLookAt, playerJet).toPosVector(), 1- CAMERA_ORIENT),
                new ExponentialSmoothVector<>(jetPosition(DirVector.zVector(), playerJet).toDirVector(), 1- CAMERA_ORIENT),
                playerJet
        );
    }

    public FollowingCamera(ExponentialSmoothVector<PosVector> eye, ExponentialSmoothVector<PosVector> focus, ExponentialSmoothVector<DirVector> up, GameEntity target) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
        this.target = target;
    }

    /**
     * @param relativePosition a position relative to target
     * @param target a target jet, where DirVector.X points forward
     * @return the position translated to world-space
     */
    private static Vector jetPosition(DirVector relativePosition, GameEntity target){
        ShadowMatrix sm = new ShadowMatrix();
        return target.getPosition().add(target.relativeToWorldSpace(relativePosition, sm), new PosVector());
    }

    /**
     * @param deltaTime the real time difference (not animation time difference)
     */
    @Override
    public void updatePosition(float deltaTime) {
        eye.updateFluent(jetPosition(relativePosition, target).toPosVector(), deltaTime);
        focus.updateFluent(jetPosition(relativeLookAt, target).toPosVector(), deltaTime);
        up.updateFluent(jetPosition(DirVector.zVector(), target).toDirVector(), deltaTime);
    }

    @Override
    public DirVector vectorToFocus(){
        return eye.current().to(target.getPosition(), new DirVector());
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
