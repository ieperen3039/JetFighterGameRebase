package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
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
    public static final float TIME_TO_LIVE = 15f;
    public static final float MASS = 0.9f;
    public static final Color4f COLOR_1 = Color4f.RED;
    public static final Color4f COLOR_2 = new Color4f(0.6f, 0.1f, 0.1f);
    public static final int NOF_PARTICLES = 25;
    public static final float EXPLOSION_CLOUD_POWER = 0.4f;
    public static final float ROTATION_REDUCTION = 0.95f;
    public static final float EXPLOSION_PARTICLE_SIZE = 0.3f;
    private static final float TRAIL_PARTICLES_PER_SEC = 250;
    private static final Color4f EXPLOSION_COLOR = new Color4f(0.6f, 0.1f, 0.1f);

    private BoosterLine trail;

    private Seeker(
            int id, PosVector position, Quaternionf rotation, DirVector velocity, GameTimer timer, SpawnReceiver game,
            AbstractJet sourceJet, MovingEntity tgt
    ) {
        super(
                id, position, rotation, velocity,
                MASS, AIR_RESIST, Toolbox.randomBetween(TIME_TO_LIVE * 0.7f, TIME_TO_LIVE), TURN_ACC, 0f, THRUST_POWER,
                ROTATION_REDUCTION, game, timer, sourceJet
        );
        if (tgt != null) {
            this.target = tgt;
            setController(new RocketAI(this, tgt, 5f, 0.1f));
        }

        trail = new BoosterLine(
                PosVector.zeroVector(), PosVector.zeroVector(), DirVector.zeroVector(),
                TRAIL_PARTICLES_PER_SEC, ClientSettings.THRUST_PARTICLE_LINGER_TIME, COLOR_1, COLOR_2, ClientSettings.THRUST_PARTICLE_SIZE
        );
    }

    @Override
    public ParticleCloud explode() {
        return Particles.explosion(position, velocity, EXPLOSION_COLOR, EXPLOSION_COLOR, EXPLOSION_CLOUD_POWER, NOF_PARTICLES, 2f, EXPLOSION_PARTICLE_SIZE);
    }

    @Override
    public void draw(GL2 gl) {
        float deltaTime = gameTimer.getRenderTime().difference();
        PosVector currPos = interpolatedPosition();
        Quaternionf currRot = interpolatedRotation();

        MatrixStack sm = new ShadowMatrix();
        toLocalSpace(sm,
                () -> entityDeposit.addParticles(trail.update(sm, DirVector.zeroVector(), 0, TRAIL_PARTICLES_PER_SEC, deltaTime)),
                currPos, currRot
        );
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
        public MovingEntity construct(SpawnReceiver game, AbstractJet src, MovingEntity tgt) {
            return new Seeker(id, position, rotation, velocity, game.getTimer(), game, src, tgt);
        }
    }
}