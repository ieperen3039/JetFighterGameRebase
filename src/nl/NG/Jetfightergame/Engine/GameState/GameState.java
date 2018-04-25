package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.EnemyFlyingTarget;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.Settings.Settings;
import nl.NG.Jetfightergame.Tools.ConcurrentArrayList;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public abstract class GameState implements Environment, PathDescription {

    protected final Collection<Particle> particles = new ConcurrentArrayList<>();
    protected final Collection<Pair<PosVector, Color4f>> lights = new ConcurrentArrayList<>();
    private Collection<MovingEntity> newEntities = new ConcurrentArrayList<>();

    private CollisionDetection collisionDetection;

    protected final Player player;

    private final GameTimer time;

    public GameState(Player player, GameTimer time) {
        this.player = player;
        this.time = time;
    }

    @Override
    public void buildScene() {
        final Collection<Touchable> staticEntities = createWorld();
        final Collection<MovingEntity> dynamicEntities = setEntities();
        collisionDetection = new CollisionDetection(dynamicEntities, staticEntities);
    }

    protected abstract Collection<Touchable> createWorld();

    protected abstract Collection<MovingEntity> setEntities();

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        final float currentTime = time.getGameTime().current();
        final float deltaTime = time.getGameTime().difference();

        if (deltaTime == 0) return;

        // update positions and apply physics
        collisionDetection.preUpdateEntities(this, deltaTime);

        // add new entities
        collisionDetection.prepareCollision(newEntities);
        newEntities.clear();

        if ((Settings.MAX_COLLISION_ITERATIONS != 0) && (deltaTime > 0))
            collisionDetection.analyseCollisions(currentTime, deltaTime, null);

        // update new state
        collisionDetection.updateEntities(currentTime);
    }

    public abstract DirVector entityNetforce(MovingEntity entity);

    @Override
    public void setLights(GL2 gl) {
        for (Pair<PosVector, Color4f> l : lights) {
            final PosVector pos = l.left;
            final Color4f color = l.right;

            gl.setLight(pos, color);

            if (Settings.SHOW_LIGHT_POSITIONS) {
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
        collisionDetection.getStaticEntities().forEach(d -> d.draw(gl));

        glDisable(GL_CULL_FACE); // TODO when new meshes are created or fixed, this should be removed
        collisionDetection.getDynamicEntities().forEach(d -> d.draw(gl));
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
        collisionDetection.cleanUp();
        System.gc();
    }

    @Override
    public PosVector getMiddleOfPath(PosVector position) {
        return PosVector.zeroVector();
    }
}
