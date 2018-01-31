package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Primitives.Particles.Particle;

import java.util.Collection;

/**
 * @author Geert van Ieperen
 * created on 31-1-2018.
 */
public interface MortalEntity {
    /**
     * @return particles resulting from this explosion
     */
    Collection<Particle> explode();

    /**
     * @return true iff this object should be removed
     */
    boolean isDead();
}
