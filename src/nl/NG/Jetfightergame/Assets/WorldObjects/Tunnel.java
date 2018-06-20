package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;

/** one curve of a Tunnel */
public class Tunnel extends StaticObject {
    private final PosVector startPosition;

    public Tunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh) {
        super(
                makeTunnel(begin, bDir, eDir, end, radius, nOfSlices, radialParts, loadMesh),
                Material.ROUGH, Color4f.BLUE
        );
        startPosition = begin;
    }

    private static Shape makeTunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh) {
        CustomShape frame = new CustomShape();

        PosVector[] lastSlice = null;
        PosVector[] currentSlice = new PosVector[radialParts];
        for (int i = 0; i < nOfSlices; i++) {
            double t = 1.0 / nOfSlices; // TODO make linear based on real length
            PosVector point = Plane.bezierPoint(begin, bDir, eDir, end, t).toPosVector();
            DirVector direction = Plane.bezierDerivative(begin, bDir, eDir, end, t);
            direction.normalize();

            // our beloved random vector
            DirVector henk = DirVector.yVector();
            if (direction.equals(henk)) henk = DirVector.zVector();

            // a vector orthogonal to the direction
            DirVector orthogonal = direction.cross(henk, henk);
            orthogonal.reducedTo(radius, orthogonal);

            float angle = (float) ((2 * Math.PI) / radialParts);
            AxisAngle4f axis = new AxisAngle4f(angle, direction);

            PosVector next = new PosVector();
            PosVector current = new PosVector(orthogonal);

            for (int j = 0; j < (radialParts - 1); j++) {
                axis.transform(current, next);

                PosVector cPos = current.add(point, new PosVector());
                if (lastSlice != null) {
                    DirVector normal = new DirVector(current);
                    normal.negate();
                    PosVector nPos = next.add(point, new PosVector());
                    frame.addQuad(cPos, nPos, lastSlice[j + 1], lastSlice[j], normal);
                }

                currentSlice[j] = cPos;
                current = next;
            }
            currentSlice[radialParts - 1] = current.add(point, new PosVector());

            if (lastSlice != null) {
                DirVector normal = orthogonal.negate(new DirVector());
                frame.addQuad(current, orthogonal.toPosVector(), lastSlice[radialParts - 1], lastSlice[0], normal);
            }

            lastSlice = currentSlice;
        }

        Logger.print(frame);
        return frame.wrapUp(loadMesh);
    }
}
