package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;

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
    void addEntity(GameEntity entity);

    /**
     * adds a collection of particles to the world
     * @param newParticles some particles
     */
    void addParticles(Collection<Particle> newParticles);
}
