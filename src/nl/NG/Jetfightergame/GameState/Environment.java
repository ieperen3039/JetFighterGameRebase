package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * an environment where the player can fly in.
 * Note that the constructor is not allowed to assume its parameters to be initialized
 * @author Geert van Ieperen
 * created on 8-1-2018.
 */
public interface Environment extends EntityReceiver {

    /**
     * @param playerJet
     */
    void addPlayerJet(AbstractJet playerJet);

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
     * initialize the scene. Make sure to call Shapes.init() for all shapes you want to initialize
     * @param collisionDetLevel 0 = no collision
     * @param loadDynamic if false, all dynamic entities are not loaded. This is required if these are managed by a server
     */
    void buildScene(int collisionDetLevel, boolean loadDynamic);

    GameEntity.State getNewSpawn();
}
