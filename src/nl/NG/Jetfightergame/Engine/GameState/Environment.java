package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;

/**
 * @author Geert van Ieperen
 * created on 8-1-2018.
 */
public interface Environment extends EntityManager {
    /**
     * update the physics of all game objects and check for collisions
     */
    @SuppressWarnings("ConstantConditions")
    void updateGameLoop();

    void setLights(GL2 gl);

    /**
     * draw all objects of the game
     */
    void drawObjects(GL2 gl);

    void drawParticles(GL2 gl);

    GameTimer getTimer();

    void cleanUp();
}
