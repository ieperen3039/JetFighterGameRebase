package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    private final DirVector shapeLocalNormal;
    private final PosVector shapeLocalHitPos;

    public float timeScalar;
    public PosVector hitPos;
    public DirVector normal;

    public Collision(){
        this(1, DirVector.zeroVector(), PosVector.zeroVector());
    }

    /**
     * @param timeScalar corrected scalar of the direction vector
     * @param normal the normal of the hit plane
     * @param hitPos actual world-space position of collision
     */
    public Collision(float timeScalar, DirVector normal, PosVector hitPos) {
        this.timeScalar = timeScalar;
        this.shapeLocalNormal = normal;
        this.shapeLocalHitPos = hitPos;
    }

    /**
     * creates a copy of the collision
     * @throws NullPointerException if {@link #convertToGlobal(MatrixStack)} has not been called on the source collision
     */
    public Collision(Collision cause) {
        this.hitPos = cause.hitPos;
        this.normal = cause.normal.negate(new DirVector());
        this.timeScalar = cause.timeScalar;
        shapeLocalNormal = null;
        shapeLocalHitPos = null;
    }

    /**
     * convert the values of the collision to global values, by providing the used matrix conversion.
     * @param ms the matrix state as how this collision was created.
     */
    public void convertToGlobal(MatrixStack ms){
        hitPos = ms.getPosition(shapeLocalHitPos);
        normal = ms.getDirection(shapeLocalNormal);
        normal.normalize();
    }

    @Override
    public int compareTo(Collision c) {
        return (c == null) ? 0 : Double.compare(c.timeScalar, timeScalar);
    }

}
