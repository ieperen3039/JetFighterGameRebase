package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class CameraManager implements Camera, Manager<CameraManager.CameraImpl> {
    private GameEntity target = null;
    private Camera instance = new PointCenteredCamera();

    @Override
    public CameraImpl[] implementations() {
        return CameraImpl.values();
    }

    public enum CameraImpl {
        PointCenteredCamera,
        FollowingCamera,
        MountedCamera
    }

    public void setTarget(GameEntity target) {
        this.target = target;
    }

    public void switchTo(CameraImpl camera){
        final PosVector eye = getEye().isScalable() ? getEye() : new PosVector(5, 0, 0);
        final DirVector up = getUpVector().isScalable() ? getUpVector() : DirVector.zVector();

        switchTo(camera, eye, target, up);
    }

    /**
     * switches camera mode, and additionally sets the camera position at the given position, and updates the target.
     * @param camera one of {@link CameraImpl}
     * @param eye position of the camera
     * @param target the game object that is the target (not necessarily the focus) of the new camera
     * @param up current upvector
     */
    public void switchTo(CameraImpl camera, PosVector eye, GameEntity target, DirVector up){
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }

        this.target = target;

        switch (camera){
            case PointCenteredCamera:
                instance = new PointCenteredCamera(eye, target.getPosition());
                break;
            case FollowingCamera:
                instance = new FollowingCamera(eye, target);
                break;
            case MountedCamera:
                if (target instanceof AbstractJet)
                    instance = new MountedCamera((AbstractJet) target);
                else
                    instance = new FollowingCamera(eye, target);
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
}