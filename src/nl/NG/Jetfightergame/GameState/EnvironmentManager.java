package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ServerNetwork.EnvironmentClass;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Geert van Ieperen created on 8-1-2018.
 *         <p>
 *         possibly we make this an instance of GameState, empty the lists upon changing world, separate GameState from
 *         GameState and reduce the GameState interface to #buildScene(). This removes a little overhead of the world
 *         instances.
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentClass> {
    private final boolean loadDynamic;
    private final int colDetLevel;

    private GameState instance;
    private SpawnReceiver deposit;
    private RaceProgress raceProgress;
    private EnvironmentClass currentType;

    /**
     * creates a switchable world, without building the scene yet.
     * @param initial the world that is built at first invocation of build()
     * @param deposit the place where all new entities are sent to
     * @param raceProgress an object that tracks rounds and checkpoints for players, required for building checkpoints
     * @param loadDynamic true if the caller is hosting a server, false if the dynamic entities shall be received by an outside source.
     * @param collisionDetectionLevel
     */
    public EnvironmentManager(
            EnvironmentClass initial, SpawnReceiver deposit, RaceProgress raceProgress, boolean loadDynamic, int collisionDetectionLevel
    ) {
        instance = (initial != null) ? initial.create() : new Void();
        currentType = initial;
        this.loadDynamic = loadDynamic;
        setContext(deposit, raceProgress);
        colDetLevel = collisionDetectionLevel;
    }

    public void build() {
        instance.buildScene(deposit, raceProgress, colDetLevel, loadDynamic);
    }

    public void setContext(SpawnReceiver deposit, RaceProgress raceProgress) {
        this.deposit = deposit;
        this.raceProgress = raceProgress;
    }

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
    public Color4f fogColor() {
        return instance.fogColor();
    }

    @Override
    public MovingEntity.State getNewSpawnPosition() {
        return instance.getNewSpawnPosition();
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return instance.entityNetforce(entity);
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
    public int getParticleCount(float currentTime) {
        return instance.getParticleCount(currentTime);
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
    public EnvironmentClass[] implementations() {
        return EnvironmentClass.values();
    }

    @Override
    public void switchTo(EnvironmentClass type) {
        GameState newInstance = (type != null) ? type.create() : new Void();
        newInstance.buildScene(deposit, raceProgress, colDetLevel, loadDynamic);
        instance.cleanUp();
        instance = newInstance;
        currentType = type;
    }

    public EnvironmentClass getCurrentType() {
        return currentType;
    }

    @Override
    public PosVector getMiddleOfPath(Collision collision) {
        return instance.getMiddleOfPath(collision);
    }

    @Nonnull
    @Override
    public Iterator<MovingEntity> iterator() {
        return instance.iterator();
    }

    @Override
    public String toString() {
        return instance.toString();
    }
}

