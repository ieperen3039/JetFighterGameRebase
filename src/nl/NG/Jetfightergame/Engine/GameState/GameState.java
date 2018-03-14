package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.CollisionDetection;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.EnemyFlyingTarget;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.AveragingQueue;
import nl.NG.Jetfightergame.Tools.ConcurrentArrayList;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public abstract class GameState implements Environment {

    private static final int COLLISION_COUNT_AVERAGE = 5;
    private AveragingQueue avgCollision = new AveragingQueue(COLLISION_COUNT_AVERAGE);
    private final Consumer<ScreenOverlay.Painter> collisionCounter = (hud) ->
            hud.printRoll(String.format("Collision count: %1.01f", avgCollision.average()));

    protected final Collection<Touchable> staticEntities = new ConcurrentArrayList<>();
    protected final Collection<MovingEntity> dynamicEntities = new ConcurrentArrayList<>();
    protected final Collection<Particle> particles = new ConcurrentArrayList<>();
    protected final Collection<Pair<PosVector, Color4f>> lights = new ConcurrentArrayList<>();
    private Collection<MovingEntity> newEntities = new ConcurrentArrayList<>();

    private CollisionDetection collisionDetection;

    protected final Player player;

    private final GameTimer time;

    public GameState(Player player, GameTimer time) {
        this.player = player;
        this.time = time;
        ScreenOverlay.addHudItem(collisionCounter);
        collisionDetection = new CollisionDetection(time, dynamicEntities, staticEntities);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        final float currentTime = time.getGameTime().current();
        final float deltaTime = time.getGameTime().difference();

        if (deltaTime == 0) return;

        // update positions and apply physics
        dynamicEntities.parallelStream()
                .forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));

        if ((Settings.MAX_COLLISION_ITERATIONS != 0) && (deltaTime > 0))
            collisionDetection.update(newEntities);

        dynamicEntities.forEach(e -> e.update(currentTime));
        dynamicEntities.removeIf(entity -> (entity instanceof MortalEntity) && ((MortalEntity) entity).isDead());
    }

    protected abstract DirVector entityNetforce(MovingEntity entity);

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
        staticEntities.forEach(d -> d.draw(gl));

        glDisable(GL_CULL_FACE); // TODO when new meshes are created or fixed, this should be removed
        dynamicEntities.forEach(d -> d.draw(gl));
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
        return new EnemyFlyingTarget(entity, () -> player.jet().interpolatedPosition());
    }

    public void cleanUp() {
        lights.clear();
        particles.clear();
        ScreenOverlay.removeHudItem(collisionCounter);
        System.gc();
    }
}
