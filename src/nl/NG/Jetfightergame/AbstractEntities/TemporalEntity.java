package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;

/**
 * @author Geert van Ieperen
 * created on 31-1-2018.
 */
public interface TemporalEntity {
    /**
     * @return particles resulting from removing this entity
     */
    ParticleCloud explode();

    /**
     * @return true iff this object should be removed
     */
    boolean isOverdue();

    static boolean isOverdue(Object target) {
        return ((target instanceof TemporalEntity) && ((TemporalEntity) target).isOverdue());
    }
}
