package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * {@author Geert van Ieperen
 *         created on 7-11-2017.
 * an object that moves, and may be influenced by other objects.
 * These methods may not influence the state of this object, this should be done with methods in
 * {@link nl.NG.Jetfightergame.Engine.Updatable}
 */
public interface MovingObject extends Touchable {

    /**
     * calculate expected position and rotation, but does not change the current state of the object.
     * This means that {@code rotation} and {@code position} are not updated
     * @param deltaTime time since last frame
     */
    void preUpdate(float deltaTime);

    /**
     * calculate effect of collision, but does not apply new position
     * <p>The procedure for computing the post-collision linear velocities {@code v'_i} and angular velocities {@code  w'_i} is as follows:</p>
     * (1) Compute the reaction impulse magnitude {@code j_r}
     * (2) Compute the reaction impulse vector {@code J_r} in terms of its magnitude {@code j_r} and contact normal {@code |n|}  using {@code  J_r = j_r * |n|}.
     * (3) Compute new linear velocities {@code  v'_i} in terms of old velocities {@code v_i}, masses {@code m_i} and reaction impulse vector {@code j_r}
     * (4) Compute new angular velocities {@code  w'_i} in terms of old angular velocities {@code  w_i}, inertia tensors {@code I_i} and reaction impulse {@code j_r}
     */
    void applyCollision();

    /**
     * checks the movement of the hitpoints of this object against the planes of 'other'.
     * @param other an object that may hit this object
     * @return true if there was a collision
     */
    boolean checkCollisionWith(Touchable other);

    /**
     * @return position of the center of mass of this object
     */
    PosVector getPosition();

    /**
     * @return movement of the center of mass of this object in world-space
     */
    DirVector getMovement();
}

