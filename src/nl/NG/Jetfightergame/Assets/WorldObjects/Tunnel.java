package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 25-4-2018.
 */
public class Tunnel implements Touchable {

    public Tunnel(PosVector ... path) {
         if ((path.length % 3) != 0){
             throw new IllegalStateException("Path must be a multiple of 3");
         }

        for (int i = 0; (i + 3) < path.length; i += 3) {
            createPathSegment(path[0], path[1], path[2], path[3]);
        }
    }

    private static void createPathSegment(PosVector begin, PosVector bDir, PosVector eDir, PosVector end) {
        DirVector direction = Plane.bezierDerivative(begin, bDir, end, eDir, 0);

        // our beloved random vector
        DirVector henk = DirVector.zVector();
        if (direction.equals(henk)) henk = DirVector.yVector();

        DirVector orthogonal = direction.cross(henk, henk);

    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {

    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {

    }

    @Override
    public void preDraw(GL2 gl) {

    }

    @Override
    public void acceptCollision(Collision cause) {

    }

}
