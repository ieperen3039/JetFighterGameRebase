package nl.NG.Jetfightergame.AbstractEntities;

/**
 * @author Geert van Ieperen
 * created on 31-1-2018.
 */
public interface MortalEntity {
    /**
     * @return true iff this object should be removed
     */
    boolean isDead();
}
