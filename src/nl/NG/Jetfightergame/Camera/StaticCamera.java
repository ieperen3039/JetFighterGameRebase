package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 * a camera that doesn't move
 */
public class StaticCamera implements Camera {

    private PosVector eye, focus;
    private DirVector up;

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
    public void updatePosition(float deltaTime) {

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
