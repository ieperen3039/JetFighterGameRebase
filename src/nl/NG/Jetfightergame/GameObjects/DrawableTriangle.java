package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
@FunctionalInterface
public interface DrawableTriangle {
    /**
     * draw this object if called in a beginEnvironment(GL_TRIANGlES) environment
     */
    void drawRaw(GL2 gl);
}
