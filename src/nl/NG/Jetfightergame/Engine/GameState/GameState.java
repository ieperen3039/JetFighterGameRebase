package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.EnemyFlyingTarget;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.ConcurrentArrayList;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public abstract class GameState implements Environment, PathDescription, NetForceProvider {

    protected final Collection<Particle> particles = new ConcurrentArrayList<>();
    protected final Collection<Pair<PosVector, Color4f>> lights = new ConcurrentArrayList<>();
    private Collection<MovingEntity> newEntities = new ConcurrentArrayList<>();

    private EntityManagement physicsEngine;

    private final GameTimer time;

    public GameState(GameTimer time) {
        this.time = time;
    }

    @Override
    public void buildScene(int collisionDetLevel, boolean loadDynamic) {
        final Collection<Touchable> staticEntities = createWorld();
        final Collection<MovingEntity> dynamicEntities = loadDynamic ? setEntities() : new ArrayList<>();

        switch (collisionDetLevel){
            case 0:
                physicsEngine = new EntityList(dynamicEntities, staticEntities);
                break;
            case 1:
                physicsEngine = new CollisionDetection(dynamicEntities, staticEntities);
                break;
            default:
                throw new UnsupportedOperationException("unsupported collision detection level:" + collisionDetLevel);
        }
    }

    @Override
    public GameEntity.State getNewSpawn() {
        return new GameEntity.State();
    }

    /**
     * @return all the static entities that are part of this world
     */
    protected abstract Collection<Touchable> createWorld();

    /**
     * @return all the dynamic entities that are standard part of this world
     */
    protected abstract Collection<MovingEntity> setEntities();

    @Override
    public void addPlayerJet(AbstractJet playerJet) {
        playerJet.set();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        final float currentTime = time.getGameTime().current();
        final float deltaTime = time.getGameTime().difference();

        if (deltaTime == 0f) return;

        // update positions and apply physics
        physicsEngine.preUpdateEntities(this, deltaTime);

        // add new entities
        physicsEngine.addEntities(newEntities);
        newEntities.clear();

        if ((ServerSettings.MAX_COLLISION_ITERATIONS != 0) && (deltaTime > 0))
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
//        Toolbox.drawAxisFrame(gl);
        physicsEngine.getStaticEntities().forEach(d -> d.draw(gl));

        glDisable(GL_CULL_FACE); // TODO when the meshes are fixed or new meshes are created, this should be removed
        physicsEngine.getDynamicEntities().forEach(d -> d.draw(gl));
    }

    @Override
    public void drawParticles(GL2 gl) {
        final Float deltaTime = time.getRenderTime().difference();

        particles.forEach(p -> p.updateRender(deltaTime));
        particles.removeIf(Particle::isOverdue);
        particles.forEach(p -> p.draw(gl));
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    @Override
    public Collection<MovingEntity> getEntities() {
        return physicsEngine.getDynamicEntities();
    }

    @Override
    public void addEntity(MovingEntity entity) {
        newEntities.add(entity);
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> entities) {
        newEntities.addAll(entities);
    }

    @Override
    public void addParticles(Collection<Particle> newParticles) {
        particles.addAll(newParticles);
    }

    @Override
    public HUDTargetable getHUDTarget(MovingEntity entity) {
        return new EnemyFlyingTarget(entity);
    }

    public void cleanUp() {
        lights.clear();
        particles.clear();
        physicsEngine.cleanUp();
        physicsEngine = null;
        System.gc();
    }

    @Override
    public PosVector getMiddleOfPath(PosVector position) {
        return PosVector.zeroVector();
    }
}
