package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Mesh;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;

import java.util.Collections;

/**
 * @author Geert van Ieperen created on 25-6-2018.
 */
public class CheckpointRing implements Shape {
    private Mesh mesh = null;
    private Plane hitPlane;

    /**
     * create a torus with 4 sides. Its center is on (0, 0, 0) and lies on the YZ plane.
     * It has a radius of 1 and the visible ring has no collision detection
     * @param radialParts   how many sections are used to construct the ring.
     * @param ringThiccness radius of the ring to its sides
     * @param loadMesh      if true, a mesh instance is loaded to the GPU
     */
    public CheckpointRing(int radialParts, float ringThiccness, boolean loadMesh) {
        DirVector orthogonal = DirVector.zVector();

        float angle = (float) ((2 * Math.PI) / radialParts);
        AxisAngle4f axis = new AxisAngle4f(angle, 1, 0, 0);

        PosVector[] ring = new PosVector[radialParts];
        DirVector[] expand = new DirVector[radialParts];

        for (int j = 0; j < radialParts; j++) {
            ring[j] = new PosVector(orthogonal);
            expand[j] = orthogonal.reducedTo(ringThiccness, new DirVector());
            axis.transform(orthogonal);
        }

        if (loadMesh) {
            CustomShape frame = new CustomShape();
            DirVector forward = new DirVector(ringThiccness, 0, 0);

            PosVector BOF = new PosVector();
            PosVector BIF = new PosVector();
            PosVector BOB = new PosVector(); // Look, it's bob!
            PosVector BIB = new PosVector();

            PosVector ringMiddle = ring[radialParts - 1];
            DirVector out = expand[radialParts - 1];
            ringMiddle.add(forward, BOF).add(out, BOF);
            ringMiddle.add(forward, BIF).sub(out, BIF);
            ringMiddle.sub(forward, BOB).add(out, BOB);
            ringMiddle.sub(forward, BIB).sub(out, BIB);

            for (int i = 0; i < ring.length; i++) {
                ringMiddle = ring[i];
                out = expand[i];
                // I = inner, O = outer, F = front, B = back
                PosVector AOF = new PosVector();
                PosVector AIF = new PosVector();
                PosVector AOB = new PosVector();
                PosVector AIB = new PosVector();

                ringMiddle.add(forward, AOF).add(out, AOF);
                ringMiddle.add(forward, AIF).sub(out, AIF);
                ringMiddle.sub(forward, AOB).add(out, AOB);
                ringMiddle.sub(forward, AIB).sub(out, AIB);

                frame.addQuad(AIF, BIF, BOF, AOF, new DirVector(1, 0, 0));
                frame.addQuad(AOF, BOF, BOB, AOB, out);
                frame.addQuad(AOB, BOB, BIB, AIB, new DirVector(-1, 0, 0));
                out.negate();
                frame.addQuad(AIF, BIF, BIB, AIB, out);

                BOF = AOF;
                BIF = AIF;
                BOB = AOB;
                BIB = AIB;
            }
            mesh = frame.asMesh();
        }


        hitPlane = new Plane(ring, new DirVector(-1, 0, 0)) {
            @Override
            protected boolean encapsulates(PosVector hitPos) {
                return hitPos.lengthSquared() < 1;
            }
        };
    }

    @Override
    public Iterable<? extends Plane> getPlanes() {
        return Collections.singleton(hitPlane);
    }

    @Override
    public Iterable<PosVector> getPoints() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void render(GL2.Painter lock) {
        mesh.render(lock);
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
}
