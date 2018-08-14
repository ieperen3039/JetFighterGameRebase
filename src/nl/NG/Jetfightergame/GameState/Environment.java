package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public interface Environment extends EntityManagement.NetForceProvider, PathDescription, EntityMapping {

    /** @return a unique place for a new player to spawn */
    EntityState getNewSpawnPosition();

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
     * allows this object to be cleaned. After calling this method, this environment should not throw exceptions that it
     * didn't throw before
     */
    default void cleanUp() {
    }

    /**
     * light of the background, alpha determines the thickness of the fog
     * @return the background-color
     */
    Color4f fogColor();

    void addGravitySource(Supplier<PosVector> position, float magnitude, float endTime);

    /**
     * public void... :D
     */
    class Void extends GameState {

        @Override
        public EntityState getNewSpawnPosition() {
            return new EntityState();
        }

        @Override
        protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
            return Collections.emptySet();
        }

        @Override
        protected Collection<EntityFactory> getInitialEntities() {
            return Collections.emptySet();
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

        @Nonnull
        @Override
        public Iterator<MovingEntity> iterator() {
            return getEntities().iterator();
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
            return Collections.emptySet();
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
        public Color4f fogColor() {
            return new Color4f(0, 0, 0, 0);
        }

        @Override
        public MovingEntity getEntity(int id) {
            return null;
        }

        @Override
        public PosVector getMiddleOfPath(Collision collision) {
            return PosVector.zeroVector();
        }

        @Override
        public DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }
    }
}
