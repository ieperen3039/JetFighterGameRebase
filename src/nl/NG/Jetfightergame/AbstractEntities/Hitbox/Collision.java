package nl.NG.Jetfightergame.AbstractEntities.Hitbox;

import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.function.Function;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    public final float timeScalar;
    public final DirVector normal;
    private final PosVector shapeLocalHitPos;
    public PosVector globalHitPos = null;

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
        this.normal = normal;
        this.shapeLocalHitPos = hitPos;
    }

    public void convertToGlobal(Function<PosVector, PosVector> converter){
        globalHitPos = converter.apply(shapeLocalHitPos);
        Toolbox.print(shapeLocalHitPos, globalHitPos);
    }

    @Override
    public int compareTo(Collision c) {
        return c == null ? 0 : Double.compare(c.timeScalar, timeScalar);
    }
}
