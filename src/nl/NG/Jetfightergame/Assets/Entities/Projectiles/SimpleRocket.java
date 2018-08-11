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
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Settings.ClientSettings.*;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleRocket extends AbstractProjectile {
    private static final float MASS = 5f;
    private static final float AIR_RESIST_COEFF = 0.01f;
    private static final float IMPACT_POWER = 5f;
    private static final float EXPLOSION_POWER = 20f;
    private static final int DENSITY = 1000;
    private static final int THRUST = 100;
    private static final Color4f THRUST_COLOR = Color4f.ORANGE;
    private static final float THRUST_PARTICLE_DENSITY = 5f;
    private BoosterLine nuzzle;

    private SimpleRocket(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                         GameTimer gameTimer, SpawnReceiver entityDeposit, MovingEntity src
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, MASS, Material.SILVER,
                AIR_RESIST_COEFF, 10, 0, 0f, THRUST,
                0.2f, entityDeposit, gameTimer, src
        );

        DirVector back = new DirVector();
        forward.negate(back).reducedTo(ClientSettings.THRUST_PARTICLE_SPEED, back).add(forward);
        PosVector pos = PosVector.zeroVector();
        nuzzle = new BoosterLine(
                pos, pos, back,
                THRUST_PARTICLE_DENSITY, ClientSettings.THRUST_PARTICLE_LINGER_TIME, THRUST_COLOR, THRUST_COLOR, ClientSettings.THRUST_PARTICLE_SIZE
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            ms.translate(-0.5f, 0, 0);
            action.accept(GeneralShapes.ARROW);
        }
        ms.popMatrix();
    }

    @Override
    public void draw(GL2 gl) {
        super.draw(gl);

        float deltaTime = gameTimer.getRenderTime().difference();
        PosVector pos = getPosition();
        DirVector back = new DirVector();
        forward.negate(back).reducedTo(ClientSettings.THRUST_PARTICLE_SPEED, back).add(forward);

        ParticleCloud cloud = nuzzle.update(pos, pos, back, deltaTime);
        entityDeposit.addParticles(cloud);
    }

    @Override
    public ParticleCloud explode() {
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(
                interpolatedPosition(), DirVector.zeroVector(),
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, EXPLOSION_POWER, DENSITY, Particles.FIRE_LINGER_TIME, FIRE_PARTICLE_SIZE
        );
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeToLive -= deltaTime;
        // sparkles
    }

    @Override
    protected PairList<PosVector, PosVector> calculateHitpointMovement() {
        PairList<PosVector, PosVector> pairs = new PairList<>(1);
        pairs.add(position, extraPosition);
        return pairs;
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(IMPACT_POWER);
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(SimpleRocket rocket) {
            super(EntityClass.SIMPLE_ROCKET, rocket);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, MovingEntity src, MovingEntity tgt) {
            return new SimpleRocket(id, position, velocity, rotation, game.getTimer(), game, src);
        }
    }
}
