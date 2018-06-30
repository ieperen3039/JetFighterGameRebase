package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.util.Collection;

/**
 * an environment where the player can fly in.
 * Note that the constructor is not allowed to assume its parameters to be initialized
 * @author Geert van Ieperen
 * created on 8-1-2018.
 */
public interface Environment {

    /** all entities added by the constructor or using {@link #addEntity(MovingEntity) */
    Collection<MovingEntity> getEntities();

    /**
     * adds an entity to this world
     * @param entity an entity, set in the appropriate position, not being controlled by outside resources
     * @see #getEntity(int)
     */
    void addEntity(MovingEntity entity);

    /**
     * searches the entity corresponding to the given ID, or null if no such entity exists
     * @param entityID the ID number of an existing entity
     * @return the entity with the given entityID, or null if no such entity exists
     */
    MovingEntity getEntity(int entityID);

    /**
     * removes an entity off this world.
     * @param entity the entity to be removed
     */
    void removeEntity(MovingEntity entity);

    /**
     * update the physics of all game objects and check for collisions
     * @param currentTime
     * @param deltaTime
     */
    @SuppressWarnings("ConstantConditions")
    void updateGameLoop(float currentTime, float deltaTime);

    /**
     * initializes the lights of this environment in the gl environment
     */
    void setLights(GL2 gl);

    /**
     * draw all objects of the game
     */
    void drawObjects(GL2 gl);

    void drawParticles(float currentTime);

    default void addEntities(Collection<? extends MovingEntity> entities) {
        entities.forEach(this::addEntity);
    }

    void addParticles(ParticleCloud newParticles);

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
     * @param deposit
     * @param collisionDetLevel 0 = no collision
     * @param loadDynamic if false, all dynamic entities are not loaded. This is required if these are managed by a server
     */
    void buildScene(SpawnReceiver deposit, int collisionDetLevel, boolean loadDynamic);

    MovingEntity.State getNewSpawn();

}
