package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * {@author Geert van Ieperen
 *         created on 7-11-2017.
 * an object that moves, and may be influenced by other objects.
 * These methods may not influence the state of this object, this should be done with methods in
 * {@link nl.NG.Jetfightergame.Engine.Updatable}
 */
public interface MovingEntity extends Touchable {

    /**
     * moves the reference frame from global space to this object and executes action.
     * every create call should preserve the matrix stack.
     * @param ms reference frame to perform transformations on
     * @param action action to perform one in local space
     * @param extrapolate true if estimations may be used (e.g. the not-rendered part)
     *                   false if the actions must be performed on parameters that no longer change
     */
    void toLocalSpace(MatrixStack ms, Runnable action, boolean extrapolate);

    default void toLocalSpace(MatrixStack ms, Runnable action){
        toLocalSpace(ms, action, false);
    }

    /**
     * calculate expected position and rotation, but does not change the current state of the object.
     * This means that {@code rotation} and {@code position} are not updated
     * @param deltaTime time since last frame
     * @param netForce
     */
    void preUpdate(float deltaTime, DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface
     * synchronization with other operations on position and rotation should be synchronized
     * @param currentTime seconds between some starttime t0 and the begin of the current gameloop
     */
    void update(float currentTime);

    /**
     * calculate effect of collision, but does not apply new position
     */
    void applyCollision(float currentTime);

    /**
     * checks the movement of the hitpoints of this object against the planes of 'other'.
     * the method implementing this must be thread-safe
     * @param other an object that may hit this object
     * @return true if there was a collision
     */
    boolean checkCollisionWith(Touchable other);

    /**
     * The entity should take care of defining a valid timestamp method to ensure correct interpolation
     * @return a copy of the position of the center of mass of this object, interpolated for a predefined timeStamp
     */
    PosVector interpolatedPosition();

    Quaternionf interpolatedRotation();

    /**
     * @return movement of the center of mass of this object in world-space
     */
    DirVector getVelocity();
}

