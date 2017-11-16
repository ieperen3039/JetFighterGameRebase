package nl.NG.Jetfightergame.GameObjects;

/**
 * {@author Geert van Ieperen
 *         created on 7-11-2017.
 */
public interface MovingObject extends Touchable {

    /**
     * calculate expected position and rotation, but does not change the current state of the object.
     * This means that {@code rotation} and {@code position} are not updated, but {@code rotationSpeed},
     * {@code netForce} and {@code movement} may be.
     * @param deltaTime time since last frame
     */
    void preUpdate(float deltaTime);

    /**
     * calculate effect of rotation and finalize actual position
     * <p>The procedure for computing the post-collision linear velocities{@code  v'_i} and angular velocities {@code  w'_i} is as follows:</p>
     * Compute the reaction impulse magnitude {@code j_r} using equation (5)
     * Compute the reaction impulse vector {@code J_r} in terms of its magnitude {@code j_r} and contact normal {@code |n|}  using {@code  J_r = j_r * |n|}.
     * Compute new linear velocities {@code  v'_i} in terms of old velocities {@code v _i}, masses {@code m_i} and reaction impulse vector {@code j _r} using equations (1a) and (1b)
     * Compute new angular velocities {@code  w'_i} in terms of old angular velocities {@code  w_i}, inertia tensors {@code I_i}and reaction impulse {@code j_r} using equations (2a) and (2b)
     *
     */
    void postUpdate();
}

