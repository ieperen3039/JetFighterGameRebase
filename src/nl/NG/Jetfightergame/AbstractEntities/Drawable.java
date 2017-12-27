package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;

/**
 * @author Geert van Ieperen
 * Created on 2-5-2017.
 */
@FunctionalInterface
public interface Drawable {
    /**
     * call the rendering method of this object, preserving the complete state of the object
     * is called in world-space
     */
    void draw(GL2 gl);
}
