package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Geert van Ieperen created on 11-12-2017.
 */
public abstract class GameState implements Environment {

    protected final Collection<ParticleCloud> particles = new ArrayList<>();
    protected final Collection<Pair<PosVector, Color4f>> lights = new CopyOnWriteArrayList<>();
    private ParticleCloud newParticles = new ParticleCloud();
    private Collection<GravitySource> gravitySources = new HashSet<>();

    private EntityManagement physicsEngine;
    private Lock addParticleLock = new ReentrantLock();
    private GameTimer gameTimer;

    /**
     * initialize the scene. Make sure to have called Shapes.init() for all shapes you want to initialize
     * @param deposit           new entities are deposited here
     * @param raceProgress      the management of the checkpoints and race-situation. The checkpoints of this world are
     *                          added upon returning
     * @param loadDynamic       if false, all dynamic entities are not loaded. This is required if these are managed by
     *                          a server
     * @param doCollDet if true, collision detection is enabled
     */
    public void buildScene(SpawnReceiver deposit, RaceProgress raceProgress, boolean loadDynamic, boolean doCollDet) {
        gameTimer = deposit.getTimer();
        final Collection<Touchable> staticEntities = createWorld(raceProgress, gameTimer);

        if (doCollDet) {
            physicsEngine = new CollisionDetection(staticEntities);

        } else {
            physicsEngine = new EntityList(staticEntities);
        }

        if (loadDynamic) {
            getInitialEntities().forEach(deposit::add);
        }
    }

    /**
     * @param raceProgress the progress tracker.
     * @param timer the timer for this world
     * @return all the static entities that are part of this world
     */
    protected abstract Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer);

    /**
     * @return all the dynamic entities that are standard part of this world
     */
    protected abstract Collection<EntityFactory> getInitialEntities();

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        float currentTime = gameTimer.getGameTime().current();
        float deltaTime = gameTimer.getGameTime().difference();

        // update positions and apply physics
        gravitySources.removeIf(s -> s.isOverdue(currentTime));
        physicsEngine.preUpdateEntities(this::getNetForce);

        if (deltaTime == 0f) return;

        if (ServerSettings.MAX_COLLISION_ITERATIONS != 0)
            physicsEngine.analyseCollisions(currentTime, deltaTime, this);

        // update new state
        physicsEngine.updateEntities(currentTime);
    }

    @Override
    public void setLights(GL2 gl) {
        for (Pair<PosVector, Color4f> l : lights) {
            final PosVector pos = l.left;
            final Color4f color = l.right;

            gl.setLight(pos, color);

            if (ClientSettings.SHOW_LIGHT_POSITIONS) {
                gl.setMaterial(Material.GLOWING, color);
                gl.pushMatrix();
                {
                    gl.translate(pos);
                    gl.scale(0.1f);
                    gl.draw(GeneralShapes.INVERSE_CUBE);
                }
                gl.popMatrix();
            }
        }
    }

    @Override
    public void drawObjects(GL2 gl) {
        if (physicsEngine == null) return;

        glDisable(GL_CULL_FACE); // TODO when the meshes are fixed or new meshes are created, this should be removed
        physicsEngine.getStaticEntities().forEach(d -> d.draw(gl));
        physicsEngine.getDynamicEntities().forEach(d -> d.draw(gl));
    }

    @Override
    public void drawParticles(float currentTime) {
        if (newParticles.readyToLoad()) {
            addParticleLock.lock();
            newParticles.writeToGL(currentTime);
            particles.add(newParticles);
            newParticles = new ParticleCloud();
            addParticleLock.unlock();
        }

        particles.removeIf(p -> p.disposeIfFaded(currentTime));
        particles.forEach(ParticleCloud::render);
    }

    @Override
    public int getParticleCount(float currentTime) {
        return particles.parallelStream()
                .mapToInt(p -> p.estParticlesAt(currentTime))
                .sum();
    }

    @Override
    public Collection<MovingEntity> getEntities() {
        return physicsEngine.getDynamicEntities();
    }

    @Override
    public Iterator<MovingEntity> iterator() {
        return physicsEngine.getDynamicEntities().iterator();
    }

    /**
     * Searches the entity corresponding to the given ID, or null if no such entity exists
     * @param entityID the ID number of an existing entity
     * @return the entity with the given entityID, or null if no such entity exists
     */
    public MovingEntity getEntity(int entityID) {
        for (MovingEntity entity : physicsEngine.getDynamicEntities()) {
            if (entity.idNumber() == entityID) {
                return entity;
            }
        }

        return null;
    }

    @Override
    public void addEntity(MovingEntity entity) {
        if (entity == null) throw new IllegalArgumentException("received null entity");
        physicsEngine.addEntity(entity);
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> entities) {
        physicsEngine.addEntities(entities);
    }

    @Override
    public void removeEntity(MovingEntity entity) {
        physicsEngine.removeEntity(entity);
    }

    @Override
    public void addParticles(ParticleCloud cloud) {
        if (cloud == null) return;
        if (cloud.readyToLoad()) {
            addParticleLock.lock();
            try {
                newParticles.addAll(cloud);
            } finally {
                addParticleLock.unlock();
            }

        } else {
            Logger.ERROR.print("Tried adding a cloud that was either already loaded, or without particles");
        }
    }

    public PosVector getMiddleOfPath(Collision collision) {
        return new PosVector();
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(super.toString());
        physicsEngine.getDynamicEntities().forEach(e -> s.append("\n\t").append(e));
        return s.toString();
    }

    @Override
    public void cleanUp() {
        physicsEngine.cleanUp();
    }

    @Override
    public void addGravitySource(Supplier<PosVector> position, float magnitude, float endTime) {
        gravitySources.add(new GravitySource(position, magnitude, endTime));
    }

    @Override
    public PosVector rayTrace(PosVector from, PosVector to) {
        return physicsEngine == null ? to : physicsEngine.rayTrace(from, to);
    }

    /**
     * a method that returns the net force on entity e, taking all gravtiy forces into account
     * @param e the entity
     * @return the net gravity forces on this entity
     */
    private DirVector getNetForce(MovingEntity e) {
        if (e instanceof AbstractJet) return entityNetforce(e);

        DirVector temp = new DirVector();
        DirVector force = entityNetforce(e);
        PosVector entPos = e.getPosition();

        for (GravitySource source : gravitySources) {
            PosVector srcPos = source.getPosition();
            DirVector toSource = entPos.to(srcPos, temp);
            float dist = toSource.length();
            float pull = dist == 0 ? 0 : source.getMagnitude() / dist;
            DirVector newGravity = toSource.reducedTo(pull, temp);
            force.add(newGravity);
        }

        return force;
    }

    private class GravitySource {
        private final Supplier<PosVector> position;
        private final float magnitude;
        private final float endTime;

        public GravitySource(Supplier<PosVector> position, float magnitude, float endTime) {
            this.position = position;
            this.magnitude = magnitude;
            this.endTime = endTime;
        }

        public boolean isOverdue(double currentTime) {
            return currentTime > endTime;
        }

        public PosVector getPosition() {
            return position.get();
        }

        public float getMagnitude() {
            return magnitude;
        }
    }
}
