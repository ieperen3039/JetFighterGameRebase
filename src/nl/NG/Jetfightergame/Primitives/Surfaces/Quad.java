package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class Quad extends Plane {

    /** ABRef, BCRef, CDRef, DARef are four reference vectors for collision detection */
    private PosVector ABRef, BCRef, CDRef, DARef;

    public Quad(PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal) {
        super(normal, list4(A, B, C, D));

        ABRef = B.subtract(A).cross(D.subtract(A));
        BCRef = C.subtract(B).cross(A.subtract(B));
        CDRef = D.subtract(C).cross(B.subtract(C));
        DARef = A.subtract(D).cross(C.subtract(D));
    }

    private static List<PosVector> list4(PosVector A, PosVector B, PosVector C, PosVector D){
        List<PosVector> points = new ArrayList<>(4);
        points.add(A);
        points.add(B);
        points.add(C);
        points.add(D);
        return points;
    }

    @Override
    protected boolean isWithin(PosVector hitPos) {
        PosVector A = boundary.get(0);
        PosVector B = boundary.get(1);
        PosVector C = boundary.get(2);
        PosVector D = boundary.get(3);

        Vector cross = B.subtract(A).cross(hitPos.subtract(A));
        if (ABRef.dot(cross) >= 0) {
            cross = C.subtract(B).cross(hitPos.subtract(B));
            if (BCRef.dot(cross) >= 0) {
                cross = D.subtract(C).cross(hitPos.subtract(C));
                if (CDRef.dot(cross) >= 0) {
                    cross = A.subtract(D).cross(hitPos.subtract(D));
                    return DARef.dot(cross) >= 0;
                }
            }
        }
        return false;
    }

    /**
     * determines whether {@code hitPos} is on the same side of AB as C
     * may be used inside {@link #isWithin(PosVector)}, but this is not efficient
     *
     * @param C      a reference point on the plane
     * @param hitPos another point on the same plane as A, B and C
     * @return true if {@code hitPos} is on the same side of AB on the plane as C
     */
    protected boolean sameSide(PosVector A, PosVector B, PosVector C, PosVector hitPos) {
        PosVector ref = B.subtract(A).cross(C.subtract(A)).toPosVector();
        Vector cross = B.subtract(A).cross(hitPos.subtract(A));
        return (ref.dot(cross) >= 0);
    }
}
