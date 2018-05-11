package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface SpawnEntityManager {
    /** adds an entity to this world */
    void addEntity(MovingEntity.SpawnEntity e, Controller playerInput);
}
