package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerDragListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerMoveListener;
import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerScrollListener;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 * The standard camera that rotates using dragging
 * some of the code originates from the RobotRace sample code of the TU/e
 */
public class PointCenteredCamera implements Camera, TrackerMoveListener, TrackerScrollListener, TrackerDragListener {

    private static final float ZOOM_SPEED = -0.1f;
    private static final float THETA_MIN = 0.01f;
    private static final float THETA_MAX = ((float) Math.PI) - 0.01f;
    private static final float PHI_MAX = (float) (2 * Math.PI);
    // Ratio of distance in pixels dragged and radial change of camera.
    private static final float DRAG_PIXEL_TO_RADIAN = -0.025f;

    /**
     * The point to which the camera is looking.
     */
    public final PosVector focus;

    /** we follow the ISO convention. Phi gives rotation, theta the height */
    private float theta;
    private float phi;
    private float vDist = 10f;

    /** cached eye position */
    private PosVector eye;

    public PointCenteredCamera() {
        this(PosVector.zeroVector(), 0, 0);
    }

    public PointCenteredCamera(PosVector eye, PosVector focus){
        DirVector focToEye = focus.to(eye, new DirVector());

        vDist = focToEye.length();
        phi = getPhi(focToEye);
        theta = getTheta(focToEye, vDist);

        this.focus = focus;
        this.eye = eye;

        registerListener();
    }

    /**
     * @param eye normalized vector to eye
     * @return phi
     */
    private static float getPhi(Vector eye) {
        return (float) Math.atan2(eye.y(), eye.x());
    }

    /**
     * @param eye normalized vector to eye
     * @param vDist distance to origin
     * @return theta
     */
    private static float getTheta(Vector eye, float vDist) {
        return (float) Math.acos(eye.z() / vDist);
    }

    public PointCenteredCamera(PosVector focus, float theta, float phi) {
        this.focus = focus;
        this.theta = theta;
        this.phi = phi;

        updatePosition(0);
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
        eye = getEyePosition();
    }

    private PosVector getEyePosition() {

        double eyeX = vDist * Math.sin(theta) * Math.cos(phi);
        double eyeY = vDist * Math.sin(theta) * Math.sin(phi);
        double eyeZ = vDist * Math.cos(theta);

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
        int s = ClientSettings.INVERT_CAMERA_ROTATION ? -1 : 1;

        theta += deltaY * DRAG_PIXEL_TO_RADIAN * s;
        phi += deltaX * DRAG_PIXEL_TO_RADIAN * s;

        theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta));
        phi = phi % PHI_MAX;
    }

    @Override
    public void mouseWheelMoved(float pixels) {
        vDist *= (ZOOM_SPEED * pixels) + 1f;
    }

    @Override
    public DirVector vectorToFocus(){
        return eye.to(focus, new DirVector());
    }

    @Override
    public PosVector getEye() {
        return eye;
    }

    @Override
    public PosVector getFocus() {
        return focus;
    }

    @Override
    public DirVector getUpVector() {
        return DirVector.zVector();
    }

    @Override
    public DirVector getVelocity() {
        return DirVector.zeroVector();
    }

    public void cleanUp(){
        MouseTracker input = MouseTracker.getInstance();
        input.removeMotionListener(this);
        input.removeDragListener(this);
        input.removeScrollListener(this, false);
    }
}
