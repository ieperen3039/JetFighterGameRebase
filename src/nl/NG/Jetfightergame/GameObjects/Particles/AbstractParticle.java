package nl.NG.Jetfightergame.GameObjects.Particles;

import nl.NG.Jetfightergame.GameObjects.DrawableTriangle;

/**
 * @author Geert van Ieperen
 *         created on 1-11-2017.
 * a drawableTriangle that is
 */
public interface AbstractParticle extends DrawableTriangle {
    /**
     * updates this particle (position, rotation) in the rendering loop
     * @param time gametime since last rendering frame
     */
    void updateRender(float time);

    /**
     * @return whether this particle is still valid
     */
    boolean alive();
}
