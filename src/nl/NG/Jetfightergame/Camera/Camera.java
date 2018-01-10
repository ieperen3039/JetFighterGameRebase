package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

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

    default void cameraLighting(GL2 gl) {}
}
