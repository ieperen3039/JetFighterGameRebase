package nl.NG.Jetfightergame.EntityGeneral.Hitbox;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class Collision implements Comparable<Collision> {
    private final DirVector shapeLocalNormal;
    private final PosVector shapeLocalHitPos;

    public final float timeScalar;
    private PosVector hitPos;
    private DirVector normal;
    private MovingEntity source;

    public Collision(){
        this(1, DirVector.zeroVector(), PosVector.zeroVector());
    }

    /**
     * @param timeScalar corrected scalar of the direction vector
     * @param normal the normal of the hit plane
     * @param hitPos local-space position of collision
     */
    public Collision(float timeScalar, DirVector normal, PosVector hitPos) {
        this.timeScalar = timeScalar;
        this.shapeLocalNormal = normal;
        this.shapeLocalHitPos = hitPos;
    }

    /**
     * creates a copy of the collision
     * @throws NullPointerException if {@link #convertToGlobal(MatrixStack, MovingEntity)} has not been called on the source collision
     */
    public Collision(Collision cause, MovingEntity source) {
        this.hitPos = cause.hitPos;
        this.normal = cause.normal.negate(new DirVector());
        this.timeScalar = cause.timeScalar;
        shapeLocalNormal = null;
        shapeLocalHitPos = null;
        this.source = source;
    }

    /**
     * convert the values of the collision to global values, by providing the used matrix conversion.
     * @param ms the matrix state as how this collision was created.
     * @param source the entity causing the collision
     */
    public void convertToGlobal(MatrixStack ms, MovingEntity source) {
        this.source = source;
        hitPos = ms.getPosition(shapeLocalHitPos);
        normal = ms.getDirection(shapeLocalNormal);
        normal.normalize();
    }

    @Override
    public int compareTo(Collision c) {
        return (c == null) ? 0 : Double.compare(c.timeScalar, timeScalar);
    }

    /**
     * @return the position of contact described by this collision in world-space
     */
    public PosVector hitPosition() {
        return new PosVector(hitPos);
    }

    /**
     * @return the normalized normal of contact of the receiving side of the collision
     */
    public DirVector normal() {
        return new DirVector(normal);
    }

    /**
     * @return the entity of the incoming side of the collision
     */
    public MovingEntity source() {
        return source;
    }

    /**
     * @return the position of collision in reference to this object (in local-space)
     */
    public PosVector getShapeLocalHitPos() {
        return shapeLocalHitPos;
    }
}
