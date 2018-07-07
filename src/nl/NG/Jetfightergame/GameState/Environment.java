package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public interface Environment extends EntityManagement.NetForceProvider, PathDescription, EntityMapping {

    /** @return a unique place for a new player to spawn */
    MovingEntity.State getNewSpawnPosition();

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

    /** draw all objects of the game */
    void drawObjects(GL2 gl);

    /** draw all particles of the game */
    void drawParticles(float currentTime);

    /** @return the estimated number of particles at the given time. */
    int getParticleCount(float currentTime);

    /** all entities added by the constructor or using {@link #addEntity(MovingEntity) */
    Collection<MovingEntity> getEntities();

    /**
     * Adds an entity to this world.
     * @param entity an entity, set in the appropriate position
     * @see #getEntity(int)
     */
    void addEntity(MovingEntity entity);

    void addEntities(Collection<? extends MovingEntity> entities);

    /**
     * Removes an entity off this world.
     * @param entity the entity to be removed
     */
    void removeEntity(MovingEntity entity);

    void addParticles(ParticleCloud cloud);

    /**
     * allows this object to be cleaned. after calling this method, this object should not be used.
     */
    void cleanUp();

    /**
     * light of the background, alpha determines the thickness of the fog
     * @return the background-color
     */
    Color4f fogColor();

    /**
     * public void... :D
     */
    class Void extends GameState {

        @Override
        public MovingEntity.State getNewSpawnPosition() {
            return new MovingEntity.State();
        }

        @Override
        protected Collection<Touchable> createWorld(RaceProgress raceProgress) {
            return Collections.EMPTY_SET;
        }

        @Override
        protected Collection<Spawn> getInitialEntities() {
            return Collections.EMPTY_SET;
        }

        @Override
        public void updateGameLoop(float currentTime, float deltaTime) {
        }

        @Override
        public void setLights(GL2 gl) {
        }

        @Override
        public void drawObjects(GL2 gl) {
        }

        @Override
        public void drawParticles(float currentTime) {
        }

        @Override
        public int getParticleCount(float currentTime) {
            return 0;
        }

        @Override
        public Collection<MovingEntity> getEntities() {
            return Collections.EMPTY_SET;
        }

        @Override
        public void addEntity(MovingEntity entity) {
        }

        @Override
        public void addEntities(Collection<? extends MovingEntity> entities) {
        }

        @Override
        public void removeEntity(MovingEntity entity) {
        }

        @Override
        public void addParticles(ParticleCloud cloud) {
        }

        @Override
        public void cleanUp() {
        }

        @Override
        public Color4f fogColor() {
            return new Color4f(0, 0, 0, 0);
        }

        @Override
        public MovingEntity getEntity(int id) {
            return null;
        }

        @Override
        public PosVector getMiddleOfPath(PosVector position) {
            return PosVector.zeroVector();
        }

        @Override
        public DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }
    }
}
