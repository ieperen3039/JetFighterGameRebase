package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/** TODO: allow quads in meshes and optimize CustomShape accordingly
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class Quad extends Plane {

    /** ABRef, BCRef, CDRef, DARef are four reference vectors for collision detection */
    private PosVector ABRef, BCRef, CDRef, DARef;

    private static PosVector tempAlpha = new PosVector();
    private static PosVector tempBeta = new PosVector();

    public Quad(PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal) {
        super(normal, new PosVector[]{A, B, C, D});

        ABRef = B.subtract(A, tempAlpha).cross(D.subtract(A, tempBeta), new PosVector());
        BCRef = C.subtract(B, tempAlpha).cross(A.subtract(B, tempBeta), new PosVector());
        CDRef = D.subtract(C, tempAlpha).cross(B.subtract(C, tempBeta), new PosVector());
        DARef = A.subtract(D, tempAlpha).cross(C.subtract(D, tempBeta), new PosVector());
    }

    @Override
    protected boolean isWithin(PosVector hitPos) {
        PosVector A = boundary[0];
        PosVector B = boundary[1];
        PosVector C = boundary[2];
        PosVector D = boundary[3];

        Vector cross = new PosVector();
        B.subtract(A, tempAlpha).cross(hitPos.subtract(A, tempBeta), cross);

        if (ABRef.dot(cross) >= 0) {
            C.subtract(B, tempAlpha).cross(hitPos.subtract(B, tempBeta), cross);
            if (BCRef.dot(cross) >= 0) {
                D.subtract(C, tempAlpha).cross(hitPos.subtract(C, tempBeta), cross);
                if (CDRef.dot(cross) >= 0) {
                    A.subtract(D, tempAlpha).cross(hitPos.subtract(D, tempBeta), cross);
                    return DARef.dot(cross) >= 0;
                }
            }
        }
        return false;
    }
}
