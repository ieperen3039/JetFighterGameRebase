package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
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
                         GameTimer gameTimer, SpawnReceiver entityDeposit, AbstractJet src
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, MASS,
                AIR_RESIST_COEFF, 10, 0, 0f, THRUST,
                0.2f, entityDeposit, gameTimer, src
        );

        DirVector back = new DirVector();
        forward.negate(back).reducedTo(ClientSettings.ROCKET_THRUST_SPEED, back).add(forward);
        nuzzle = new BoosterLine(
                PosVector.zeroVector(), PosVector.zeroVector(), back,
                THRUST_PARTICLE_DENSITY, THRUST_PARTICLE_LINGER_TIME, THRUST_COLOR, THRUST_COLOR, THRUST_PARTICLE_SIZE,
                gameTimer);
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

    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.ROUGH);

        DirVector back = new DirVector();
        forward.negate(back).reducedTo(ClientSettings.ROCKET_THRUST_SPEED, back).add(forward);

        toLocalSpace(gl, () -> entityDeposit.add(
                nuzzle.update(gl, DirVector.zeroVector(), 0, THRUST_PARTICLE_DENSITY)
        ));
    }

    @Override
    public ParticleCloud explode() {
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(
                getPosition(), DirVector.zeroVector(),
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, EXPLOSION_POWER, DENSITY, Particles.FIRE_LINGER_TIME, FIRE_PARTICLE_SIZE
        );
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeToLive -= deltaTime;
        // sparkles
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
        other.impact(1.5f, IMPACT_POWER);
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(SimpleRocket rocket) {
            super(EntityClass.SIMPLE_ROCKET, rocket);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, AbstractJet src, MovingEntity tgt) {
            return new SimpleRocket(id, position, velocity, rotation, game.getTimer(), game, src);
        }
    }
}
