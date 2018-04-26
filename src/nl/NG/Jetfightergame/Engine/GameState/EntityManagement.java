package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.PathDescription;

import java.util.Collection;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public interface EntityManagement {

    /**
     * calls the pre-update on all entities
     * @param gameState provides the netforce on the objects
     * @param deltaTime the time difference since last update
     */
    void preUpdateEntities(NetForceProvider gameState, float deltaTime);

    /**
     * checks and resolves all collisions that occurred in the given timeperiod
     * @param currentTime the current game-loop time
     * @param deltaTime the in-game time difference from the last call to this method
     * @param environment the path contained in the static parts of this
     */
    void analyseCollisions(float currentTime, float deltaTime, PathDescription environment);

    /**
     * adds the new entities to the collision detection, and cleans out dead entities from the arrays.
     * @param newEntities a set of new entities to be added to the collision detection. The set is unmodified.
     */
    void addEntities(Collection<MovingEntity> newEntities);

    Collection<Touchable> getStaticEntities();

    Collection<MovingEntity> getDynamicEntities();

    /**
     * calls {@link nl.NG.Jetfightergame.AbstractEntities.GameEntity#update(float)}
     * @param currentTime gametime
     */
    void updateEntities(float currentTime);

    void cleanUp();
}
