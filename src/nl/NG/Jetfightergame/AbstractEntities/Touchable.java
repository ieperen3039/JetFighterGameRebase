package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreators.Shape;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 6-11-2017.
 */
public interface Touchable extends Drawable {

    default RigidBody getRigidBody(Map<Touchable, RigidBody> finalCollisions, float deltaTime) {
        RigidBody item;
        if (finalCollisions.containsKey(this)) {
            item = finalCollisions.get(this);
        } else {
            item = getFinalCollision(deltaTime);
            finalCollisions.put(this, item);
        }

        finalCollisions.getOrDefault(this, item);
        return item;
    }

    /**
     * moves the reference frame from local space to each shape, executing {@code action} on every shape.
     * every create call should preserve the matrix stack.
     * If this object is moving, this applies to the current position
     * @param ms reference frame to perform transformations on
     * @param action actions to execute for every Shape
     */
    void create(MatrixStack ms, Consumer<Shape> action);

    /**
     * moves the reference frame from global space to this object and executes action.
     * Every create call should preserve the matrix stack.
     * If this object is moving, this applies to the current position
     * @see MovingEntity#toLocalSpace(MatrixStack, Runnable, boolean)
     * @param ms reference frame to perform transformations on
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
     * prepare gl object for drawing this object (material properties, shaders...).
     * This method should called inside the draw method
     */
    void preDraw(GL2 gl);

    /**
     * process the effect of another object colliding on this object.
     * @param cause the data of the collision from causer's perspective
     */
    void acceptCollision(Collision cause);

    /**
     * returns the first collision of this object
     */
    RigidBody getFinalCollision(float deltaTime);
}
