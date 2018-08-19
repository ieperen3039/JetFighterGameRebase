package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public interface EntityManagement {

    /**
     * calls the pre-update on all entities
     * @param gravity provides the netforce on the objects
     * @param deltaTime the time difference since last update
     */
    void preUpdateEntities(NetForceProvider gravity, float deltaTime);

    /**
     * checks and resolves all collisions that occurred in the given timeperiod
     * @param currentTime the current game-loop time
     * @param deltaTime the in-game time difference from the last call to this method
     * @param path the path contained in the static parts of this
     */
    void analyseCollisions(float currentTime, float deltaTime, PathDescription path);

    /**
     * adds the new entities to the collision detection
     * @param newEntities a set of new entities to be added to the collision detection. The set is unmodified.
     */
    void addEntities(Collection<? extends MovingEntity> newEntities);

    /**
     * adds one new entity to the collision detection. This will at last be in effect in the next loop
     * @param entity
     */
    void addEntity(MovingEntity entity);

    /** @return an unmodifiable collection of the static entities in this world */
    Collection<Touchable> getStaticEntities();

    /** @return an unmodifiable collection of the moving entities in this world */
    Collection<MovingEntity> getDynamicEntities();

    /**
     * calls {@link MovingEntity#update(float)}
     * @param currentTime gametime
     */
    void updateEntities(float currentTime);

    void cleanUp();

    void removeEntity(MovingEntity entity);

    /**
     * @param from start position
     * @param to   end position
     * @return the position that can be reached along the line of from - to without hitting anything
     */
    PosVector rayTrace(PosVector from, PosVector to);

    /**
     * @author Geert van Ieperen created on 26-4-2018.
     */
    interface NetForceProvider {
        /**
         * @param entity any dynamic entity in this world
         * @return the force of gravity (or whatever) that naturally acts on this entity
         */
        DirVector entityNetforce(MovingEntity entity);
    }
}
