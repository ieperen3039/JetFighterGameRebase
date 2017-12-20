package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.EntityDefinitions.GameEntity;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class FollowingCamera implements Camera {
    private static final DirVector relativePosition = new DirVector(-5, 0, 3);
    private static final DirVector relativeLookAt = relativePosition.add(new DirVector(10, 0, -1), new DirVector());
    public static final float CAMERA_CATCHUP = 0.4f;
    /**
     * The position of the camera.
     */
    private final ExponentialSmoothVector<PosVector> eye;
    private final GameEntity target;

    public FollowingCamera(GameEntity target) {
        this(jetPosition(relativePosition, target).toPosVector(), target);
    }

    public FollowingCamera(PosVector eye, GameEntity playerJet) {
        this(
                new ExponentialSmoothVector<>(eye, 1-CAMERA_CATCHUP),
                playerJet
        );
    }

    public FollowingCamera(ExponentialSmoothVector<PosVector> eye, GameEntity target) {
        this.eye = eye;
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
        return jetPosition(relativeLookAt, target).toPosVector();
    }

    @Override
    public DirVector getUpVector() { // TODO smooooth
        return jetPosition(DirVector.zVector(), target).toDirVector();
    }
}
