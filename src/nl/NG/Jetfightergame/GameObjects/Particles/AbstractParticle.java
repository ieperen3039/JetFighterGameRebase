package nl.NG.Jetfightergame.GameObjects.Particles;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.Renderable;

/**
 * @author Geert van Ieperen
 *         created on 1-11-2017.
 * a drawableTriangle that is
 */
public interface AbstractParticle extends Renderable {
    /**
     * updates this particle (position, rotation) in the rendering loop
     * @param time gametime since last rendering frame
     */
    void updateRender(float time);

    void draw(GL2 gl);

    /**
     * @return whether this particle is still valid
     */
    boolean alive();
}
