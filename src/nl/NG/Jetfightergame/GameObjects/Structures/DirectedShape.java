package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Engine.GLMatrix.AxisBasedGL;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface DirectedShape extends Shape {

    void setSource(AxisBasedGL ms, PosVector source);

    void setTarget(AxisBasedGL ms, PosVector target);
}
