package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import static java.awt.event.KeyEvent.*;

/**
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public class SimpleKeyCamera implements Camera {

    private static final KeyTracker input = KeyTracker.getInstance();

    static {
        input.addKey(VK_LEFT);
        input.addKey(VK_RIGHT);
        input.addKey(VK_UP);
        input.addKey(VK_DOWN);
    }

    /**
     * The up vector.
     */
    public final DirVector up;
    /**
     * The position of the camera.
     */
    public PosVector eye;
    /**
     * The point to which the camera is looking.
     */
    public PosVector focus;

    public SimpleKeyCamera() {
        this(new PosVector(0, 5, 2), PosVector.zeroVector(), DirVector.zVector());
    }

    public SimpleKeyCamera(PosVector eye, PosVector focus, DirVector up) {
        this.eye = eye;
        this.focus = focus;
        this.up = up;
    }

    @Override
    public void updatePosition(float deltaTime) {
        DirVector movement = DirVector.zeroVector();
        DirVector left = DirVector.zVector().cross(eye.toDirVector(), new DirVector());
        if (input.isPressed(VK_LEFT)) {
            movement = movement.add(left, new DirVector());
        }
        if (input.isPressed(VK_RIGHT)) {
            movement = movement.add(left.scale(-1, new DirVector()), new DirVector());
        }
        if (input.isPressed(VK_UP)) {
            movement = movement.add(DirVector.zVector(), new DirVector());
        }
        if (input.isPressed(VK_DOWN)){
            movement = movement.add(DirVector.zVector().scale(-1, new DirVector()), new DirVector());
        }
        eye = eye.add(movement.scale(deltaTime * 1f, new DirVector()), new PosVector());
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
        return up;
    }
}
