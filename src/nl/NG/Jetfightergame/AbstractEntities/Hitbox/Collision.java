package nl.NG.Jetfightergame.AbstractEntities.Hitbox;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    public final double timeToImpact;
    public final DirVector normal;
    public final PosVector hitPos;

    public Collision(){
        this(1, DirVector.zeroVector(), PosVector.zeroVector());
    }

    /**
     * @param timeToImpact corrected scalar of the direction vector
     * @param normal the normal of the hit plane
     * @param hitPos
     */
    public Collision(double timeToImpact, DirVector normal, PosVector hitPos) {
        this.timeToImpact = timeToImpact;
        this.normal = normal;
        this.hitPos = hitPos;
    }

    @Override
    public int compareTo(Collision c) {
        return c == null ? 0 : Double.compare(c.timeToImpact, timeToImpact);
    }
}
