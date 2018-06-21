package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;

/** one curve of a Tunnel */
public class Tunnel extends StaticObject {
    private final PosVector startPosition;
    private final PosVector endPosition;

    public Tunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh) {
        super(
                makeTunnel(begin, bDir, eDir, end, radius, nOfSlices, radialParts, loadMesh),
                Material.ROUGH, Color4f.BLUE
        );
        startPosition = begin;
        endPosition = end;
    }

    private static Shape makeTunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh) {
        CustomShape frame = new CustomShape(PosVector.zeroVector(), true);

        PosVector[] lastSlice = null;
        for (int i = 0; i < (nOfSlices + 1); i++) {
            PosVector[] currentSlice = new PosVector[radialParts];

            double t = (double) i / nOfSlices; // TODO make linear based on real length
            PosVector point = Plane.bezierPoint(begin, bDir, eDir, end, t).toPosVector();
            DirVector direction = Plane.bezierDerivative(begin, bDir, eDir, end, t);
            direction.normalize();
            frame.setMiddle(point);

            // our beloved random vector
            DirVector henk = DirVector.zVector();
            if (direction.equals(henk)) henk = DirVector.yVector();

            // a vector orthogonal to the direction
            DirVector orthogonal = direction.cross(henk, henk);
            orthogonal.reducedTo(radius, orthogonal);

            float angle = (float) ((2 * Math.PI) / radialParts);
            AxisAngle4f axis = new AxisAngle4f(angle, direction);

            for (int j = 0; j < radialParts; j++) {
                currentSlice[j] = point.add(orthogonal, new PosVector());
                axis.transform(orthogonal);
            }

            if (lastSlice != null) {
                for (int j = 0; j < radialParts; j++) {
                    int nextIndex = (j + 1) % radialParts;
                    frame.addQuad(currentSlice[j], currentSlice[nextIndex], lastSlice[nextIndex], lastSlice[j]);
                }
            }

            lastSlice = currentSlice;
        }
        return frame.wrapUp(loadMesh);
    }
}
