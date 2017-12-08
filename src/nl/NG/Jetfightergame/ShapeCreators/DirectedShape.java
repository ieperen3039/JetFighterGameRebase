package nl.NG.Jetfightergame.ShapeCreators;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface DirectedShape extends Shape {

    void setSource(MatrixStack ms, PosVector source);

    void setTarget(MatrixStack ms, PosVector target);
}
