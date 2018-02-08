package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public interface Camera {

    DirVector vectorToFocus();

    void updatePosition(float deltaTime);

    PosVector getEye();

    PosVector getFocus();

    DirVector getUpVector();
}
