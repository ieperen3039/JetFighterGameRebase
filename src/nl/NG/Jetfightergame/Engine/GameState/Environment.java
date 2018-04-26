package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * an environment where the player can fly in.
 * Note that the constructor is not allowed to assume its parameters to be initialized
 * @author Geert van Ieperen
 * created on 8-1-2018.
 */
public interface Environment extends EntityManager {

    /**
     * @param player a new player to be added to this world
     */
    void addPlayer(Player player);

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

    /**
     * allows this object to be cleaned.
     * after calling this method, this object should not be used.
     */
    void cleanUp();

    /**
     * light of the background, alpha determines the thickness of the fog
     * @return the background-color
     */
    Color4f fogColor();

    /**
     * initialize the scene
     */
    void buildScene();
}
