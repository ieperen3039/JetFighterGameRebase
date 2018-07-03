package nl.NG.Jetfightergame.AbstractEntities;

/**
 * @author Geert van Ieperen. Created on 3-7-2018.
 */
@FunctionalInterface
public interface EntityMapping {
    /**
     * Searches the entity corresponding to the given ID, or null if no such entity exists
     * @param id the ID number of an existing entity
     * @return the entity with the given entityID, or null if no such entity exists
     */
    MovingEntity getEntity(int id);
}
