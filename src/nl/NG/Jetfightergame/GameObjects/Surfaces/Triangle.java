package nl.NG.Jetfightergame.GameObjects.Surfaces;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class Triangle extends Plane {

    /** ABRef, BCRef, CARef are three reference vectors for collision detection */
    private PosVector ABRef, BCRef, CARef;

    public Triangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        super(normal, list3(A, B, C));

        ABRef = B.subtract(A).cross(C.subtract(A));
        BCRef = C.subtract(B).cross(A.subtract(B));
        CARef = A.subtract(C).cross(B.subtract(C));
    }

    public Triangle(PosVector A, PosVector B, PosVector C, PosVector middle) {
        this(A, B, C, getNormalVector(A, B, C, middle));
    }

    private static List<PosVector> list3(PosVector A, PosVector B, PosVector C){
        List<PosVector> points = new ArrayList<>(3);
        points.add(A);
        points.add(B);
        points.add(C);
        return points;
    }

    /**
     * computes in optimized fashion whether the given point lies inside the triangle
     * @param hitPos a point on this plane
     * @return true if the point is within the boundaries
     */
    @Override
    protected boolean isWithin(PosVector hitPos) {
        PosVector A = boundary.get(0);
        PosVector B = boundary.get(1);
        PosVector C = boundary.get(2);

        Vector cross = B.subtract(A).cross(hitPos.subtract(A));
        if (ABRef.dot(cross) >= 0) {
            cross = C.subtract(B).cross(hitPos.subtract(B));
            if (BCRef.dot(cross) >= 0) {
                cross = A.subtract(C).cross(hitPos.subtract(C));
                return CARef.dot(cross) >= 0;
            }
        }
        return false;
    }
}
