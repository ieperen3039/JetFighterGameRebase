package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;

/**
 * @author Geert van Ieperen
 * created on 31-1-2018.
 */
public interface MortalEntity {
    /**
     * @return particles resulting from this explosion
     */
    ParticleCloud explode();

    /**
     * @return true iff this object should be removed
     */
    boolean isDead();

    static boolean isDead(Object target){
        return ((target instanceof MortalEntity) && ((MortalEntity) target).isDead());
    }
}
