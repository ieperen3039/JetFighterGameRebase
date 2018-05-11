package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;

import java.util.Collection;

/**
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public interface EntityManager {

    /**
     * adds an moving entity to the game's collision detection and rendering
     * @param entity the new entity
     */
    void addEntity(MovingEntity entity);

    default void addEntities(Collection<? extends MovingEntity> entities){
        for (MovingEntity entity : entities) {
            addEntity(entity);
        }
    }

    /**
     * adds a collection of particles to the world
     * @param newParticles some particles
     */
    void addParticles(Collection<Particle> newParticles);

    /**
     * connects this entity to be visible on the hud, be it friend or foe.
     * @param entity the entity to be added
     * @return the object, for the calling class to manage and delete.
     * Be aware that this must be deleted, or both the calling class and this target will not be cleaned.
     */
    HUDTargetable getHUDTarget(MovingEntity entity);

    GameTimer getTimer();

    Collection<MovingEntity> getEntities();

//    /**
//     * adds the sound effect to the sound engine.
//     * Properties of the source should still be managed by the calling method
//     * @param sound a new sound instance
//     */
//    void addSoundEffect(AudioSource sound);
}
