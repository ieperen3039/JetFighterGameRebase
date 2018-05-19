package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Scenarios.CollisionLaboratory;
import nl.NG.Jetfightergame.Assets.Scenarios.ExplosionLaboratory;
import nl.NG.Jetfightergame.Assets.Scenarios.MissionSnake;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Toolbox;
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
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentManager.Worlds> {

    private final GameTimer time;
    private Environment instance;
    private SpawnReceiver deposit;

    public EnvironmentManager(GameTimer time) {
        this.time = time;
    }

    @Override
    public void addEntity(MovingEntity entity) {
        instance.addEntity(entity);
    }

    @Override
    public MovingEntity removeEntity(int entityID) {
        return instance.removeEntity(entityID);
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
        instance = new PlayerJetLaboratory(time);
        instance.buildScene(deposit, ServerSettings.COLLISION_DETECTION_LEVEL, true);
    }

    @Override
    public void buildScene(SpawnReceiver deposit, int collisionDetLevel, boolean loadDynamic) {
        instance.buildScene(deposit, collisionDetLevel, true);
    }

    @Override
    public GameEntity.State getNewSpawn() {
        return instance.getNewSpawn();
    }

    public enum Worlds {
        PlayerJetLaboratory,
        CollisionLaboratory,
        ExplosionLaboratory,
        SnakeLevel
    }

    @Override
    public void updateGameLoop() {
        instance.updateGameLoop();
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
    public void drawParticles() {
        instance.drawParticles();
    }

    @Override
    public GameTimer getTimer() {
        return instance.getTimer();
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
                instance = new CollisionLaboratory(time);
                break;
            case PlayerJetLaboratory:
                instance = new PlayerJetLaboratory(time);
                break;
            case ExplosionLaboratory:
                instance = new ExplosionLaboratory(time);
                break;
            case SnakeLevel:
                instance = new MissionSnake(time);
                break;
            default:
                Toolbox.printError("Environment not properly registered: " + implementation + " (did we forget a break statement?)");
                instance = new Void();
        }

        instance.buildScene(deposit, ServerSettings.COLLISION_DETECTION_LEVEL, true);
    }


    private class Void extends GameState {
        /**
         * public void... :D
         */
        public Void() {
            super(time);
        }

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

