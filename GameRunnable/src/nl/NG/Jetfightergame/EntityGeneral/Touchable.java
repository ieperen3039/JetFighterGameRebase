package nl.NG.Jetfightergame.EntityGeneral;

import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 6-11-2017.
 */
public interface Touchable extends Drawable {

    /**
     * moves the reference frame from local space to each shape, executing {@code action} on every shape. every create
     * call should preserve the matrix stack. If this object is moving, this applies to the current position
     * @param ms     reference frame to perform transformations on
     * @param action actions to execute for every Shape
     */
    void create(MatrixStack ms, Consumer<Shape> action);

    /**
     * moves the reference frame from global space to this object and executes action. Every create call should preserve
     * the matrix stack. If this object is moving, this applies to the current position
     * @param ms     reference frame to perform transformations on
     * @param action action to perform one in local space
     */
    void toLocalSpace(MatrixStack ms, Runnable action);

    /**
     * draw the object using the native create function and the shape.drawObjects functions
     */
    @Override
    default void draw(GL2 gl) {
        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, (() -> create(gl, painter)));
    }

    /**
     * prepare gl object for drawing this object (material properties, shaders...). This method should called inside the
     * draw method
     */
    void preDraw(GL2 gl);

    default void acceptCollision(Collision cause) {
    }

    /**
     * react on collision with a projectile
     * @param factor
     * @param duration magnitude of the impact
     */
    default void impact(float factor, float duration) {
    }

    /**
     * distance of the farthest vertex of this object
     * @return the vertex P for which P.to(getPosition()) is maximized in world-space
     */
    float getRange();

    /**
     * @return the position of this object in the next timestamp. This value is bound to change after calls to
     *         {@link nl.NG.Jetfightergame.GameState.CollisionDetection}
     */
    PosVector getExpectedMiddle();
}
