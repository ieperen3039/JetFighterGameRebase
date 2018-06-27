package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen created on 25-6-2018.
 */
public class Ring extends StaticObject {

    public Ring(PosVector position, DirVector direction, float radius) {
        super(GeneralShapes.CHECKPOINT, Material.GOLD, Color4f.WHITE, position, new Vector3f(radius), Toolbox.xTo(direction));
    }


    /**
     * create a torus with 4 sides. Its center is on (0, 0, 0) and lies on the YZ plane.
     * @param radius        the radius of the middle of the ring
     * @param radialParts   how many sections are used to construct the ring.
     * @param ringThiccness radius of the ring to its sides
     * @param loadMesh      if true, a mesh instance is loaded to the GPU
     * @return the resulting shape
     */
    public static Shape makeRing(float radius, int radialParts, float ringThiccness, boolean loadMesh) {
        // our beloved random vector
        PosVector position = PosVector.zeroVector();
        DirVector orthogonal = new DirVector(0, 0, radius);

        float angle = (float) ((2 * Math.PI) / radialParts);
        AxisAngle4f axis = new AxisAngle4f(angle, 1, 0, 0);

        DirVector forward = new DirVector(ringThiccness, 0, 0);
        CustomShape frame = new CustomShape();

        PosVector BOF = null;
        PosVector BIF = null;
        PosVector BOB = null; // Look, it's bob!
        PosVector BIB = null;
        PosVector ring = new PosVector();
        DirVector out = new DirVector();

        for (int j = 0; j <= radialParts; j++) {
            position.add(orthogonal, ring);
            orthogonal.reducedTo(ringThiccness, out);

            // I = inner, O = outer, F = front, B = back
            PosVector AOF = new PosVector();
            PosVector AIF = new PosVector();
            PosVector AOB = new PosVector();
            PosVector AIB = new PosVector();

            ring.add(forward, AOF).add(out, AOF);
            ring.add(forward, AIF).sub(out, AIF);
            ring.sub(forward, AOB).add(out, AOB);
            ring.sub(forward, AIB).sub(out, AIB);

            if (j > 0) {
                frame.addQuad(AIF, BIF, BOF, AOF, new DirVector(1, 0, 0));
                frame.addQuad(AOF, BOF, BOB, AOB, out);
                frame.addQuad(AOB, BOB, BIB, AIB, new DirVector(-1, 0, 0));
                out.negate();
                frame.addQuad(AIF, BIF, BIB, AIB, out);
            }

            BOF = AOF;
            BIF = AIF;
            BOB = AOB;
            BIB = AIB;

            axis.transform(orthogonal);
        }

        return frame.wrapUp(loadMesh);
    }
}
