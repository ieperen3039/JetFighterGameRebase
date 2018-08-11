package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.*;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractProjectile {
    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0f;

    private SimpleBullet(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                         GameTimer gameTimer, SpawnReceiver entityDeposit, MovingEntity src
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, MASS, Material.SILVER,
                AIR_RESIST_COEFF, 10, 0, 0f, 0, 0.2f, entityDeposit, gameTimer, src
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
        other.impact(5);
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
