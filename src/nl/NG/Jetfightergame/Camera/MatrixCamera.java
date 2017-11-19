package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Engine.GLFWGameEngine;
import nl.NG.Jetfightergame.Engine.Window.GLFWWindow;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Yoeri Poels, Jorren Hendriks
 *
 * Class to represent a camera in 3d space.
 */
public class MatrixCamera implements Camera {

    private GLFWGameEngine engine;
    private GLFWWindow window;

    public MatrixCamera(GLFWGameEngine engine) {
        super();
        this.engine = engine;
        this.window = engine.getWindow();
    }

    @Override
    public DirVector vectorToFocus() {
        return null;
    }

    @Override
    public void updatePosition(float deltaTime) {

    }

    @Override
    public PosVector getEye() {
        return null;
    }

    @Override
    public PosVector getFocus() {
        return null;
    }

    @Override
    public DirVector getUpVector() {
        return null;
    }
}