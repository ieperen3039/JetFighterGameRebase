package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

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

    public Triangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        super(normal, new PosVector[]{A, B, C});

        ABRef = B.subtract(A, tempAlpha).cross(C.subtract(A, tempBeta), new PosVector());
        BCRef = C.subtract(B, tempAlpha).cross(A.subtract(B, tempBeta), new PosVector());
        CARef = A.subtract(C, tempAlpha).cross(B.subtract(C, tempBeta), new PosVector());
    }

    public Triangle(PosVector A, PosVector B, PosVector C, PosVector middle) {
        this(A, B, C, getNormalVector(A, B, C, middle));
    }

    /**
     * computes in optimized fashion whether the given point lies inside the triangle
     * @param hitPos a point on this plane
     * @return true if the point is within the boundaries
     */
    @Override
    protected boolean isWithin(PosVector hitPos) {
        PosVector A = boundary[0];
        PosVector B = boundary[1];
        PosVector C = boundary[2];

        B.subtract(A, tempAlpha).cross(hitPos.subtract(A, tempBeta), cross);

        if (ABRef.dot(cross) >= 0) {
            C.subtract(B, tempAlpha).cross(hitPos.subtract(B, tempBeta), cross);
            if (BCRef.dot(cross) >= 0) {
                A.subtract(C, tempAlpha).cross(hitPos.subtract(C, tempBeta), cross);
                return CARef.dot(cross) >= 0;
            }
        }
        return false;
    }
}
