package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * Possibly split into a control-part and an engine-part {@author Geert van Ieperen created on 7-11-2017. an object that
 * moves, and may be influenced by other objects.
 */
public interface MovingEntity extends Touchable {

    float getKineticEnergy(DirVector vector);

    /**
     * moves the reference frame from global space to this object and executes action. every create call should preserve
     * the matrix stack.
     * @param ms          reference frame to perform transformations on
     * @param action      action to perform one in local space
     * @param extrapolate true if estimations may be used (e.g. the not-rendered part) false if the actions must be
     *                    performed on parameters that no longer change
     */
    void toLocalSpace(MatrixStack ms, Runnable action, boolean extrapolate);

    default void toLocalSpace(MatrixStack ms, Runnable action) {
        toLocalSpace(ms, action, false);
    }

    /**
     * calculate expected position and rotation, but does not change the current state of the object. This means that
     * {@code rotation} and {@code position} are not updated
     * @param deltaTime time since last frame
     * @param netForce
     */
    void preUpdate(float deltaTime, DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface. Other operations on
     * position and rotation should be synchronized
     * @param currentTime seconds between some starttime t0 and the begin of the current gameloop
     */
    void update(float currentTime);

    /**
     * checks the movement of the hitpoints of this object against the planes of 'other'. the method implementing this
     * must be thread-safe
     * @param other     an object that may hit this object
     * @param deltaTime
     * @return true if there was a collision. This also means that the other has a collision as well
     */
    boolean checkCollisionWith(Touchable other, float deltaTime);

    /**
     * @return a copy of the position of the center of mass of this object, interpolated for a predefined timeStamp
     * @implNote The entity should take care of defining a valid timestamp method to ensure correct
     *         interpolation
     */
    PosVector interpolatedPosition();

    /**
     * @return a copy of the rotation of this object, interpolated for a predefined timeStamp
     * @implNote The entity should take care of defining a valid timestamp method to ensure correct
     *         interpolation
     */
    Quaternionf interpolatedRotation();

    /**
     * @return a copy of the movement of the center of mass of this object in world-space
     */
    DirVector getVelocity();

    /** adds a position state for rendering on the specified time */
    void addStatePoint(float currentTime, PosVector hitPosition, Quaternionf rotation);

    /**
     * @return the mass of this object in kilograms.
     */
    float getMass();

    /**
     * @return a copy of the position of the center of mass of this object in world-space
     */
    PosVector getPosition();

    /**
     * @return the object's current rotation
     */
    Quaternionf getRotation();

    /**
     * distance of the farthest vertex of this object
     * @return the vertex P for which P.to(getPosition()) is maximized in world-space
     */
    float getRange();

    /**
     * @return the position of this object in the next timestamp. This value is bound to change after calls to {@link
     *         nl.NG.Jetfightergame.GameState.CollisionDetection}
     */
    PosVector getExpectedPosition();

    /**
     * applies a change in velocity by applying the given momentum to the velocity. This may only be applied between
     * calls to {@link #preUpdate(float, DirVector)} and {@link #update(float)}
     * @param direction the normalized direction in which the force is applied
     * @param energy    the energy in Newton to be applied to the gravity center of this entity
     * @param deltaTime time difference of the current gameloop
     */
    void applyJerk(DirVector direction, float energy, float deltaTime);

    /** @return an unique number given by the server */
    int idNumber();

    /**
     * a description of a moving entity
     */
    class Spawn {
        public final EntityClass type;
        public final PosVector position;
        public final Quaternionf rotation;
        public final DirVector velocity;

        public Spawn(EntityClass type, PosVector position, Quaternionf rotation, DirVector velocity) {
            this.type = type;
            this.position = position;
            this.rotation = rotation;
            this.velocity = velocity;
        }

        public Spawn(EntityClass type, GameEntity.State state) {
            this(type, state.position(0), state.rotation(0), state.velocity());
        }

        public MovingEntity construct(SpawnReceiver game, Controller input) {
            return type.construct(Identity.next(), game, input, position, rotation, velocity);
        }
    }
}

