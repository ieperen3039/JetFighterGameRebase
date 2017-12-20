package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Camera.FollowingCamera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.EntityDefinitions.GameEntity;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class CameraManager implements Camera {
    private GameEntity target = null;
    private Camera instance;

    enum CameraImpl {
        CAMERA_POINT_CENTERED,
        CAMERA_FOLLOWING
    }

    public CameraManager() {
        instance = new PointCenteredCamera();
    }

    public void setTarget(GameEntity target) {
        this.target = target;
    }

    public void switchTo(CameraImpl camera){
        switchTo(camera, getEye(), target);
    }

    /**
     * switches camera mode, and additionally sets the camera position at the given position, and updates the target.
     * @param camera one of {@link CameraImpl}
     * @param eye position of the camera
     * @param target the game object that is the target (not necessarily the focus) of the new camera
     */
    public void switchTo(CameraImpl camera, PosVector eye, GameEntity target){
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }

        this.target = target;

        switch (camera){
            case CAMERA_POINT_CENTERED:
                instance = new PointCenteredCamera(eye, target.getPosition());
                break;
            case CAMERA_FOLLOWING:
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
