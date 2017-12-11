package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Camera.FollowingCamera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerListener;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class CameraManager implements Camera {
    private GameObject target = null;
    private Camera instance;

    enum CameraImpl {
        CAMERA_POINT_CENTERED,
        CAMERA_FOLLOWING
    }

    public CameraManager() {
        instance = new PointCenteredCamera();
    }

    public void setTarget(GameObject target) {
        this.target = target;
    }

    public void switchTo(CameraImpl camera){
        if (instance instanceof TrackerListener) {
            ((TrackerListener) instance).cleanUp();
        }

        switch (camera){
            case CAMERA_POINT_CENTERED:
                instance = new PointCenteredCamera(getEye(), target.getPosition(), getUpVector());
                break;
            case CAMERA_FOLLOWING:
                instance = new FollowingCamera(getEye(), target);
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
