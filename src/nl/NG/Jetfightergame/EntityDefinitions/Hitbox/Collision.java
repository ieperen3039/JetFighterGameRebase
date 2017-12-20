package nl.NG.Jetfightergame.EntityDefinitions.Hitbox;

import nl.NG.Jetfightergame.Vectors.DirVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    public final double timeToImpact;
    public final DirVector normal;

    public Collision(){
        this(1, DirVector.zeroVector());
    }

    /**
     *  @param timeToImpact corrected scalar of the direction vector
     * @param normal the normal of the hit plane
     */
    public Collision(double timeToImpact, DirVector normal) {
        this.timeToImpact = timeToImpact;
        this.normal = normal;
    }

    @Override
    public int compareTo(Collision c) {
        return c == null ? 0 : Double.compare(c.timeToImpact, timeToImpact);
    }
}
