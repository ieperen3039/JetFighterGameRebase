package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ServerNetwork.EnvironmentClass;
import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 8-1-2018.
 *         <p>
 *         possibly we make this an instance of GameState, empty the lists upon changing world, separate GameState from
 *         GameState and reduce the GameState interface to #buildScene(). This removes a little overhead of the world
 *         instances.
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentClass> {
    private static final EnvironmentClass[] VALUES = EnvironmentClass.values();
    private final boolean loadDynamic;

    private GameState instance;
    private SpawnReceiver deposit;
    private RaceProgress raceProgress;
    private EnvironmentClass currentType;
    private boolean doCollDet;

    /**
     * creates a switchable world, without building the scene yet.
     * @param initial the world that is built at first invocation of build()
     * @param deposit the place where all new entities are sent to
     * @param raceProgress an object that tracks rounds and checkpoints for players, required for building checkpoints
     * @param loadDynamic true if the caller is hosting a server, false if the dynamic entities shall be received by an outside source.
     * @param doCollDet
     */
    public EnvironmentManager(
            EnvironmentClass initial, SpawnReceiver deposit, RaceProgress raceProgress, boolean loadDynamic, boolean doCollDet
    ) {
        instance = (initial != null) ? initial.create() : null;
        currentType = initial;
        this.loadDynamic = loadDynamic;
        this.deposit = deposit;
        this.raceProgress = raceProgress;
        this.doCollDet = doCollDet;
    }

    public void build() {
        instance.buildScene(deposit, raceProgress, loadDynamic, doCollDet);
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
    public void addGravitySource(Supplier<PosVector> position, float magnitude, float endTime) {
        instance.addGravitySource(position, magnitude, endTime);
    }

    @Override
    public EntityState getNewSpawnPosition() {
        return instance.getNewSpawnPosition();
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return instance.entityNetforce(entity);
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
    public void switchTo(int n) {
        switchTo(VALUES[n]);
    }

    @Override
    public int nrOfImplementations() {
        return VALUES.length;
    }

    @Override
    public void switchTo(EnvironmentClass type) {
        raceProgress.reset();
        GameState oldInst = instance;
        instance = (type != null) ? type.create() : new Void();
        build();
        if (oldInst != null) oldInst.cleanUp();
        currentType = type;
        System.gc();
    }

    public EnvironmentClass getCurrentType() {
        return currentType;
    }

    @Override
    public PosVector getMiddleOfPath(Collision collision) {
        return instance.getMiddleOfPath(collision);
    }

    @Override
    public Iterator<MovingEntity> iterator() {
        return instance.iterator();
    }

    @Override
    public String toString() {
        return instance.toString();
    }

    @Override
    public PosVector rayTrace(PosVector from, PosVector to) {
        return instance.rayTrace(from, to);
    }

    @Override
    public AudioFile backgroundMusic() {
        return instance.backgroundMusic();
    }
}

