package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_1;
import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_2;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractProjectile {
    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0f;
    public static final int TIME_TO_LIVE = 5;

    private SimpleBullet(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                         GameTimer gameTimer, SpawnReceiver entityDeposit, AbstractJet src
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, MASS,
                AIR_RESIST_COEFF, TIME_TO_LIVE, 0, 0f, 0, 0.1f, entityDeposit, gameTimer, src
        );
    }

    @Override
    public ParticleCloud explode() {
        timeToLive = 0;
        PosVector pos = getPosition();
        DirVector vel = DirVector.zeroVector();
        entityDeposit.add(new AudioSource(Sounds.seekerPop, pos, 0.5f, 1f));
        return Particles.explosion(
                pos, vel,
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2,
                10f, 5, 1, 1f
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        action.accept(GeneralShapes.ROCKET);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.SILVER);
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
        other.impact(1.1f, 5);
    }

    public static class Factory extends EntityFactory {
        public Factory() {
            super();
        }

        public Factory(SimpleBullet bullet) {
            super(EntityClass.SIMPLE_BULLET, bullet);
        }

        public Factory(EntityState state) {
            super(EntityClass.SIMPLE_BULLET, state, 0);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new SimpleBullet(id, position, velocity, rotation, game.getTimer(), game, null);
        }
    }
}
