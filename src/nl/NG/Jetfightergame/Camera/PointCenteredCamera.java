package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Controllers.MouseTracker;
import nl.NG.Jetfightergame.Controllers.TrackerDragListener;
import nl.NG.Jetfightergame.Controllers.TrackerMoveListener;
import nl.NG.Jetfightergame.Controllers.TrackerScrollListener;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 * The standard camera that rotates using dragging
 * some of the code originates from the RobotRace sample code of the TU/e
 */
public class PointCenteredCamera implements Camera, TrackerMoveListener, TrackerScrollListener, TrackerDragListener {

    /**
     * The position of the camera.
     */
    public final TrackedVector<PosVector> eye;
    /**
     * The point to which the camera is looking.
     */
    public final PosVector focus;
    /**
     * The up vector.
     */
    public final DirVector up;
    private float theta;
    private float phi;
    private float vDist = Settings.VDIST;

    public PointCenteredCamera() {
        this(PosVector.O, DirVector.Z, 0, 0);
    }

    public PointCenteredCamera(PosVector focus, DirVector up, float theta, float phi) {
        this.focus = focus;
        this.up = up;
        this.theta = theta;
        this.phi = phi;

        this.eye = new TrackedVector<>(getEyePosition());

        MouseTracker input = MouseTracker.getInstance();
        input.addMotionListener(this);
        input.addDragListener(this);
        input.addScrollListener(this, false);
    }

    @Override
    public void updatePosition(float deltaTime) {
        eye.update(getEyePosition());
    }

    private PosVector getEyePosition() {
        int i = Settings.INVERT_CAMERA_ROTATION ? 1 : -1;

        double eyeX = (vDist * Math.cos(theta * i)) * Math.cos(phi);
        double eyeY = (vDist * Math.sin(theta * i)) * Math.cos(phi);
        double eyeZ = vDist * Math.sin(phi);

        return new PosVector(eyeX, eyeY, eyeZ).add(focus);
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        theta += deltaX * Settings.DRAG_PIXEL_TO_RADIAN * -1;
        phi = Math.max(Settings.PHI_MIN,
                Math.min(Settings.PHI_MAX,
                        phi + deltaY * Settings.DRAG_PIXEL_TO_RADIAN ));
    }

    @Override
    public void mouseDragged(int deltaX, int deltaY) {
        mouseMoved(deltaX, deltaY);
    }

    @Override
    public void mouseWheelMoved(float pixels) {
        vDist -= pixels;
    }

    @Override
    public DirVector vectorToFocus(){
        return eye.current().to(focus);
    }

    @Override
    public PosVector getEye() {
        return getEyePosition();
    }

    @Override
    public PosVector getFocus() {
        return focus;
    }

    @Override
    public DirVector getUpVector() {
        return up;
    }
}
