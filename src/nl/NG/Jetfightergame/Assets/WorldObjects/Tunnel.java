package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Primitives.Plane;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * one curve of a Tunnel
 * @deprecated
 */
public class Tunnel implements Touchable {
    private final PosVector startPosition;
    private final PosVector endPosition;
    private final Material material = Material.ROUGH;
    private final Color4f color;

    public Tunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh, Color4f color) {
        this.color = color;
        List<CustomShape> shape = makeTunnel(begin, bDir, eDir, end, radius, nOfSlices, radialParts, loadMesh);
        startPosition = begin;
        endPosition = end;
    }

    private static List<CustomShape> makeTunnel(PosVector begin, PosVector bDir, PosVector eDir, PosVector end, float radius, int nOfSlices, int radialParts, boolean loadMesh) {
        List<CustomShape> result = new ArrayList<>();

        PosVector[] lastSlice = null;
        for (int i = 0; i < (nOfSlices + 1); i++) {
            PosVector[] currentSlice = new PosVector[radialParts];

            double t = (double) i / nOfSlices; // TODO make linear based on real length
            PosVector point = Plane.bezierPoint(begin, bDir, eDir, end, t).toPosVector();
            DirVector direction = Plane.bezierDerivative(begin, bDir, eDir, end, t);
            direction.normalize();

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
                CustomShape frame = new CustomShape(point, true);
                for (int j = 0; j < radialParts; j++) {
                    int nextIndex = (j + 1) % radialParts;
                    frame.addQuad(currentSlice[j], currentSlice[nextIndex], lastSlice[nextIndex], lastSlice[j]);
                }
                result.add(frame);
            }

            lastSlice = currentSlice;
        }

        return result;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
//            action.accept(source);
        }
        ms.popMatrix();
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        action.run();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(material, color);
    }

    @Override
    public void acceptCollision(Collision cause) {

    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return null;
    }
}
