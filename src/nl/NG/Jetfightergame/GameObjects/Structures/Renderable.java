package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;

/**
 * @author Geert van Ieperen
 *         created on 17-11-2017.
 */
public interface Renderable {
    void render(GL2.Painter lock);
}
