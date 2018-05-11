package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface GameServer {
    /** adds an entity to this world */
    void spawnEntity(MovingEntity.SpawnEntity e, Controller playerInput);

    /** pauses the server  */
    void pause();

    /** intiates the shutdown sequence of the server */
    void shutDown();

    /** starts the server if it is enabled */
    void unPause();
}
