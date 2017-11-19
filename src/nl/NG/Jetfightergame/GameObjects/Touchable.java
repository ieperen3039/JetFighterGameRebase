package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 6-11-2017.
 */
public interface Touchable extends Drawable {

    /**
     * moves the reference frame from local space to each shape, executing {@code action} on every shape.
     * every create call should preserve the matrix stack.
     * @param ms reference frame to perform transformations on
     * @param action actions to execute for every Shape
     * @param takeStable false if the actions are done on the (stable) current points,
     *                   true if the actions are done on the (unstable) extrapolated points
     */
    void create(GL2 ms, Consumer<Shape> action, boolean takeStable);

    /**
     * moves the reference frame from global space to this object and executes action
     * every create call should preserve the matrix stack.
     * @param ms reference frame to perform transformations on
     * @param action action to perform one in local space
     * @param takeStable false if estimations may be used (e.g. the not rendered part)
     *                   true if the actions must be performed on parameters taht no longer change
     */
    void toLocalSpace(GL2 ms, Runnable action, boolean takeStable);

    /**
     * checks the collision with one specific object, and update this object accordingly
     * @param other an object that may hit this object
     */
    void checkCollisionWith(Touchable other);

    /**
     * drawObjects the object using the native create function and the shape.drawObjects functions
     */
    @Override
    default void draw(GL2 gl) {
        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, (() -> create(gl, painter, false)), false);
    }

    /**
     * prepare gl object for drawing this object (material properties, shaders...)
     */
    void preDraw(GL2 gl);
}
