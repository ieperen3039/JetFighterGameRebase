package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.EntityManager;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameTimer;

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

    int getNumberOfLights();
}
