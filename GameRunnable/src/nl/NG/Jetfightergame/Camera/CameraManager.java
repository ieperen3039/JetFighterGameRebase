package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class CameraManager implements Camera, Manager<CameraManager.CameraImpl> {
    private static final CameraImpl[] VALUES = CameraImpl.values();
    private Player target = null;
    private Camera instance = null;
    private Environment gameState;

    @Override
    public CameraImpl[] implementations() {
        return CameraImpl.values();
    }

    public enum CameraImpl {
        PointCenteredCamera,
        FollowingCamera,
        SpectatorFollowing,
        MountedCamera
    }

    @Override
    public void switchTo(int n) {
        switchTo(VALUES[n]);
    }

    @Override
    public int nrOfImplementations() {
        return VALUES.length;
    }

    public void switchTo(CameraImpl camera) {
        final PosVector thisEye = instance.getEye();
        final DirVector thisUp = instance.getUpVector();
        final DirVector up = thisUp.isScalable() ? thisUp : DirVector.zVector();

        switchTo(camera, thisEye, target, gameState, up);
    }

    /**
     * switches camera mode, and additionally sets the camera position at the given position, and updates the target.
     * @param camera one of {@link CameraImpl}
     * @param eye position of the camera
     * @param target the game object that is the target (not necessarily the focus) of the new camera
     * @param gameState
     * @param up current upvector
     */
    public void switchTo(CameraImpl camera, PosVector eye, Player target, Environment gameState, DirVector up) {
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }
        this.target = target;
        this.gameState = gameState;

        AbstractJet jet = target.jet();
        DirVector vecToFocus = (instance == null) ? eye.to(jet.getPosition(), new DirVector()) : vectorToFocus();

        switch (camera){
            case PointCenteredCamera:
                instance = new PointCenteredCamera(eye, jet.getPosition());
                break;

            case FollowingCamera:
                instance = new FollowingCamera(eye, target, up, vecToFocus, gameState);
                break;

            case MountedCamera:
                instance = new MountedCamera(jet);
                break;

            case SpectatorFollowing:
                instance = new SpectatorFollowingCamera(
                        eye, target, up, jet.getPosition(), gameState
                );
                break;

            default:
                throw new UnsupportedOperationException("unknown enum: " + camera);
        }
    }

    @Override
    public DirVector vectorToFocus() {
        return instance.vectorToFocus();
    }

    @Override
    public void updatePosition(float deltaTime) {
        instance.updatePosition(deltaTime);
    }

    @Override
    public PosVector getEye() {
        return instance.getEye();
    }

    @Override
    public PosVector getFocus() {
        return instance.getFocus();
    }

    @Override
    public DirVector getUpVector() {
        return instance.getUpVector();
    }

    @Override
    public DirVector getVelocity() {
        return instance.getVelocity();
    }
}
