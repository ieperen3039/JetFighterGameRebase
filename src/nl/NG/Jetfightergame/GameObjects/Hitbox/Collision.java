package nl.NG.Jetfightergame.GameObjects.Hitbox;

import nl.NG.Jetfightergame.Vectors.DirVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable {
    public final double timeToImpact;
    public final DirVector normal;

    public Collision(){
        this(1, DirVector.O);
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
    public int compareTo(Object o) {
        if (o instanceof Collision) {
            return Double.compare(((Collision) o).timeToImpact, timeToImpact);
        } else if (o == null){
            return 0;
        }
        throw new ClassCastException("compared Collision with non-Collision object");
    }
}
