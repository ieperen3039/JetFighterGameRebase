package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.EntityState;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 17-7-2018.
 */
public class Seeker extends AbstractProjectile {
    public static final int THRUST_POWER = 300;
    public static final float TURN_ACC = 5f;
    public static final float AIR_RESIST = 0.05f;
    public static final float TIME_TO_LIVE = 10f;
    public static final float MASS = 0.9f;
    public static final Color4f COLOR_1 = new Color4f(0.1f, 0, 0);
    public static final Color4f COLOR_2 = new Color4f(0.6f, 0.1f, 0.1f);
    public static final int NOF_PARTICLES = 25;
    public static final float EXPLOSION_CLOUD_POWER = 0.4f;
    public static final float ROTATION_REDUCTION = 0.8f;
    public static final float EXPLOSION_PARTICLE_SIZE = 0.3f;
    private static final float TRAIL_PARTICLES_PER_SEC = 200;
    private static final Color4f THRUST_COLOR = Color4f.RED;

    private BoosterLine trail;

    private Seeker(
            int id, PosVector position, Quaternionf rotation, DirVector velocity, GameTimer timer, SpawnReceiver game,
            MovingEntity sourceJet, MovingEntity tgt
    ) {
        super(
                id, position, rotation, velocity,
                MASS, AIR_RESIST, Toolbox.randomBetween(TIME_TO_LIVE * 0.7f, TIME_TO_LIVE), TURN_ACC, 0f, THRUST_POWER,
                ROTATION_REDUCTION, game, timer, sourceJet
        );
        if (tgt != null) {
            this.target = tgt;
            setController(new RocketAI(this, tgt, 5f));
        }

        trail = new BoosterLine(
                PosVector.zeroVector(), PosVector.zeroVector(), DirVector.zeroVector(),
                TRAIL_PARTICLES_PER_SEC, ClientSettings.THRUST_PARTICLE_LINGER_TIME, THRUST_COLOR, THRUST_COLOR, ClientSettings.THRUST_PARTICLE_SIZE
        );
    }

    @Override
    public ParticleCloud explode() {
        return Particles.explosion(position, velocity, COLOR_1, COLOR_2, EXPLOSION_CLOUD_POWER, NOF_PARTICLES, 2f, EXPLOSION_PARTICLE_SIZE);
    }

    @Override
    public void draw(GL2 gl) {
        // instead of drawing, add particles for the trail
        float deltaTime = gameTimer.getRenderTime().difference();
        toLocalSpace(gl, () -> entityDeposit.addParticles(
                trail.update(gl, DirVector.zeroVector(), deltaTime)
        ));
    }

    @Override
    public void preDraw(GL2 gl) {
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(1);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(EntityState state, float fraction, MovingEntity source, MovingEntity target) {
            super(EntityClass.SEEKER, state, fraction, source, target);
        }

        public Factory(Seeker seeker) {
            super(EntityClass.SEEKER, seeker);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, MovingEntity src, MovingEntity tgt) {
            return new Seeker(id, position, rotation, velocity, game.getTimer(), game, src, tgt);
        }
    }
}