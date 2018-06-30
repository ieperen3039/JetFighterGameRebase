package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Scenarios.CollisionLaboratory;
import nl.NG.Jetfightergame.Assets.Scenarios.ExplosionLaboratory;
import nl.NG.Jetfightergame.Assets.Scenarios.MissionSnake;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 8-1-2018.
 *
 * possibly we make this an instance of GameState, empty the lists upon changing world, separate GameState from Environment
 * and reduce the Environment interface to #buildScene(). This removes a little overhead of the world instances.
 *
 * @deprecated This is not usable for usage in a server-client model
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentManager.Worlds> {

    private Environment instance;
    private SpawnReceiver deposit;

    @Override
    public void addEntity(MovingEntity entity) {
        instance.addEntity(entity);
    }

    @Override
    public MovingEntity getEntity(int entityID) {
        return instance.getEntity(entityID);
    }

    @Override
    public void removeEntity(MovingEntity entity) {
        instance.removeEntity(entity);
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> entities) {
        instance.addEntities(entities);
    }

    @Override
    public void addParticles(ParticleCloud newParticles) {
        instance.addParticles(newParticles);
    }

    @Override
    public Color4f fogColor(){
        return instance.fogColor();
    }

    public void init(SpawnReceiver deposit) {
        this.deposit = deposit;
        instance = new PlayerJetLaboratory();
        instance.buildScene(deposit, ServerSettings.COLLISION_DETECTION_LEVEL, true);
    }

    @Override
    public void buildScene(SpawnReceiver deposit, int collisionDetLevel, boolean loadDynamic) {
        instance.buildScene(deposit, collisionDetLevel, true);
    }

    @Override
    public MovingEntity.State getNewSpawn() {
        return instance.getNewSpawn();
    }

    public enum Worlds {
        PlayerJetLaboratory,
        CollisionLaboratory,
        ExplosionLaboratory,
        SnakeLevel
    }

    @Override
    public void updateGameLoop(float currentTime, float deltaTime) {
        instance.updateGameLoop(currentTime, deltaTime);
    }

    @Override
    public void setLights(GL2 gl) {
        instance.setLights(gl);
    }

    @Override
    public void drawObjects(GL2 gl) {
        instance.drawObjects(gl);
    }

    @Override
    public void drawParticles(float currentTime) {
        instance.drawParticles(currentTime);
    }

    @Override
    public Collection<MovingEntity> getEntities() {
        return instance.getEntities();
    }

    @Override
    public void cleanUp() {
        instance.cleanUp();
    }


    @Override
    public Worlds[] implementations() {
        return Worlds.values();
    }

    @Override
    public void switchTo(Worlds implementation) {
        instance.cleanUp();
        ClientSettings.SPECTATOR_MODE = false;

        switch (implementation) {
            case CollisionLaboratory:
                instance = new CollisionLaboratory();
                break;
            case PlayerJetLaboratory:
                instance = new PlayerJetLaboratory();
                break;
            case ExplosionLaboratory:
                instance = new ExplosionLaboratory();
                break;
            case SnakeLevel:
                instance = new MissionSnake();
                break;
            default:
                Logger.printError("Environment not properly registered: " + implementation + " (did we forget a break statement?)");
                instance = new Void();
        }

        instance.buildScene(deposit, ServerSettings.COLLISION_DETECTION_LEVEL, true);
    }


    /**
     * private void Void()... :D
     */
    private class Void extends GameState {
        @Override
        protected Collection<Touchable> createWorld() {
            return Collections.EMPTY_SET;
        }

        @Override
        protected Collection<MovingEntity> setEntities(SpawnReceiver deposit) {
            return Collections.EMPTY_SET;
        }

        @Override
        public DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }

        @Override
        public Color4f fogColor(){
            return new Color4f(0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}

