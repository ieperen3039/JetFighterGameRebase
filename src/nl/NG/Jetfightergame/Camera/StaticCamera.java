package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 * a camera that moves according to its parameters
 */
public class StaticCamera implements Camera {

    private PosVector eye, focus;
    private DirVector up;

    /**
     * this class will not change the given vectors under any circumstance
     * @param eye reference to position. this camera changes position whenever {@code eye} is changed
     * @param focus reference to focus. this camera changes its direction whenever {@code focus} is changed
     * @param up reference to up-direction. this camera changes orientation whenever {@code up} is changed
     */
    public StaticCamera(PosVector eye, PosVector focus, DirVector up) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
    }

    @Override
    public DirVector vectorToFocus() {
        return eye.to(focus, new DirVector());
    }

    @Override
    public void updatePosition(TrackedFloat timer) {

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
        return up;
    }
}
