package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class Triangle extends Plane {

    /** ABRef, BCRef, CARef are three reference vectors for collision detection */
    private PosVector ABRef, BCRef, CARef;

    /** reserved space for collision detection */
    private PosVector tempAlpha = new PosVector();
    private PosVector tempBeta = new PosVector();
    private PosVector cross = new PosVector();

    /**
     * the vectors must be supplied in counterclockwise ordering
     */
    public Triangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        super(new PosVector[]{A, B, C}, normal);

        ABRef = B.sub(A, tempAlpha).cross(C.sub(A, tempBeta), new PosVector());
        BCRef = C.sub(B, tempAlpha).cross(A.sub(B, tempBeta), new PosVector());
        CARef = A.sub(C, tempAlpha).cross(B.sub(C, tempBeta), new PosVector());
    }

    public static Triangle createTriangle(PosVector A, PosVector B, PosVector C, DirVector direction) {
        final DirVector normal = getNormalVector(A, B, C);

        if (normal.dot(direction) >= 0) {
            return new Triangle(A, B, C, normal);
        } else {
            normal.negate();
            return new Triangle(C, B, A, normal);
        }
    }

    /**
     * computes in optimized fashion whether the given point lies inside the triangle
     * @param hitPos a point on this plane
     * @return true if the point is within the boundaries
     */
    @Override
    protected boolean encapsulates(PosVector hitPos) {
        PosVector A = boundary[0];
        PosVector B = boundary[1];
        PosVector C = boundary[2];

        B.sub(A, tempAlpha).cross(hitPos.sub(A, tempBeta), cross);

        if (ABRef.dot(cross) >= 0) {
            C.sub(B, tempAlpha).cross(hitPos.sub(B, tempBeta), cross);
            if (BCRef.dot(cross) >= 0) {
                A.sub(C, tempAlpha).cross(hitPos.sub(C, tempBeta), cross);
                return CARef.dot(cross) >= 0;
            }
        }
        return false;
    }
}
