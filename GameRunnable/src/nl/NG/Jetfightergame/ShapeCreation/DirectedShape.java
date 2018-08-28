package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface DirectedShape extends Shape {

    void setSource(MatrixStack ms, PosVector source);

    void setTarget(MatrixStack ms, PosVector target);
}
