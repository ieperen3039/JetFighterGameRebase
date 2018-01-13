package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerDragListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerMoveListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerScrollListener;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 * The standard camera that rotates using dragging
 * some of the code originates from the RobotRace sample code of the TU/e
 */
public class PointCenteredCamera implements Camera, TrackerMoveListener, TrackerScrollListener, TrackerDragListener {

    private static final float ZOOM_SPEED = -0.1f;
    public static float PHI_MIN = (-(float) Math.PI / 2f) + 0.01f;
    public static float PHI_MAX = ((float) Math.PI / 2f) - 0.01f;
    // Ratio of distance in pixels dragged and radial change of camera.
    public static float DRAG_PIXEL_TO_RADIAN = 0.025f;

    /**
     * The position of the camera.
     */
    public final TrackedVector<PosVector> eye;
    /**
     * The point to which the camera is looking.
     */
    public final PosVector focus;
    private float theta;
    private float phi;
    private float vDist = 10f;

    public PointCenteredCamera() {
        this(PosVector.zeroVector(), 0, 0);
    }

    public PointCenteredCamera(PosVector eye, PosVector focus){
        DirVector focToEye = focus.to(eye, new DirVector());
        DirVector cameraDir = focToEye.normalize(new DirVector());

        vDist = focToEye.length();
        phi = getPhi(cameraDir);
        theta = getTheta(cameraDir, phi);

        this.focus = focus;
        this.eye = new TrackedVector<>(getEyePosition());

        registerListener();
    }

    /**
     * @param eye normalized vector to eye
     * @return phi
     */
    private static float getPhi(Vector eye) {
        return (float) Math.asin(eye.z());
    }

    /**
     * @param eye normalized vector to eye
     * @param phi
     * @return theta
     */
    private static float getTheta(Vector eye, float phi) {
        int i = Settings.INVERT_CAMERA_ROTATION ? 1 : -1;
        return (float) (Math.acos(eye.x()/Math.cos(phi)) * i);
    }

    public PointCenteredCamera(PosVector focus, float theta, float phi) {
        this.focus = focus;
        this.theta = theta;
        this.phi = phi;

        this.eye = new TrackedVector<>(getEyePosition());

        registerListener();
    }

    private void registerListener() {
        MouseTracker input = MouseTracker.getInstance();
        input.addMotionListener(this);
        input.addDragListener(this);
        input.addScrollListener(this, false);
    }

    /**
     * updates the eye position
     * @param deltaTime not used
     */
    @Override
    public void updatePosition(float deltaTime) {
        eye.update(getEyePosition());
    }

    private PosVector getEyePosition() {
        int i = Settings.INVERT_CAMERA_ROTATION ? 1 : -1;

        double eyeX = vDist * Math.cos(theta * i) * Math.cos(phi);
        double eyeY = vDist * Math.sin(theta * i) * Math.cos(phi);
        double eyeZ = vDist * Math.sin(phi);

        final PosVector eye = new PosVector((float) eyeX, (float) eyeY, (float) eyeZ);
        return eye.add(focus, eye);
    }

    /** move is inverse of dragging */
    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        mouseDragged(-deltaX, -deltaY);
    }

    @Override
    public void mouseDragged(int deltaX, int deltaY) {
        theta -= deltaX * DRAG_PIXEL_TO_RADIAN;
        phi = Math.max(PHI_MIN,
                Math.min(PHI_MAX,
                        phi + (deltaY * DRAG_PIXEL_TO_RADIAN)));
    }

    @Override
    public void mouseWheelMoved(float pixels) {
        vDist *= (ZOOM_SPEED * pixels) + 1f;
    }

    @Override
    public DirVector vectorToFocus(){
        return eye.current().to(focus, new DirVector());
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
        return DirVector.zVector();
    }

    public void cleanUp(){
        MouseTracker input = MouseTracker.getInstance();
        input.removeMotionListener(this);
        input.removeDragListener(this);
        input.removeScrollListener(this, false);
    }
}
