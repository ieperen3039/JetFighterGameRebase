package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 17-7-2018.
 */
public class Seeker extends AbstractProjectile {
    public static final int THRUST_POWER = 200;
    public static final float TURN_ACC = 3f;
    public static final float AIR_RESIST = 0.02f;
    public static final float TIME_TO_LIVE = 30f;
    public static final float MASS = 1f;
    public static final Color4f COLOR_1 = new Color4f(0.1f, 0, 0);
    public static final Color4f COLOR_2 = new Color4f(0.6f, 0.1f, 0.1f);
    public static final int NOF_PARTICLES = 10;
    public static final float EXPLOSION_CLOUD_POWER = 1f;
    public static final float ROTATION_REDUCTION = 0.8f;
    public static final float PARTICLE_SIZE = 0.3f;


    private Seeker(
            int id, PosVector position, Quaternionf rotation, DirVector velocity, GameTimer timer, SpawnReceiver game,
            MovingEntity sourceJet, MovingEntity tgt
    ) {
        super(
                id, position, rotation, velocity,
                1f, MASS, Material.GLOWING, AIR_RESIST, TIME_TO_LIVE, TURN_ACC, THRUST_POWER,
                ROTATION_REDUCTION, game, timer, sourceJet
        );
        if (tgt != null) setTarget(tgt);
    }

    @Override
    public ParticleCloud explode() {
        return Particles.explosion(position, velocity, COLOR_1, COLOR_2, EXPLOSION_CLOUD_POWER, NOF_PARTICLES, 4f, PARTICLE_SIZE);
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(1);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        //TODO create shape
        ms.pushMatrix();
        {
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            ms.translate(-0.5f, 0, 0);
            action.accept(GeneralShapes.ARROW);
        }
        ms.popMatrix();
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(State state, float fraction, MovingEntity source, MovingEntity target) {
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