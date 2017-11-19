package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface DirectedShape extends Shape {

    void setSource(GL2 ms, PosVector source);

    void setTarget(GL2 ms, PosVector target);
}
