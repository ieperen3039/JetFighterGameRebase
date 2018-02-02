package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;

/**
 * @author Geert van Ieperen
 *         created on 1-11-2017.
 * a drawableTriangle that is
 */
public interface Particle {

    /**
     * updates this particle (position, rotation) in the rendering loop
     * @param deltaTime gametime since last rendering frame
     */
    void updateRender(float deltaTime);

    void draw(GL2 gl);

    /**
     * @return whether this particle is no longer valid
     */
    boolean isOverdue();
}
