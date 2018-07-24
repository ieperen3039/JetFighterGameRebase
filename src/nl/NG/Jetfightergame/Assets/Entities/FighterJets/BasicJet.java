package nl.NG.Jetfightergame.Assets.Entities.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 11-11-2017.
 */
public class BasicJet extends AbstractJet {
    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 800f;
    public static final float BRAKE_POWER = 3f; // air resist is multiplied with this
    public static final float MASS = 40f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 2f;
    public static final float PITCH_POWER = 3f;
    public static final float ROLL_POWER = 3f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.1f;

    private final Shape shape;
    private final PosVector shapeMiddle;
    private final float shapeRange;

    private BasicJet(
            int id, PosVector initialPosition, Quaternionf initialRotation, GameTimer renderTimer,
            SpawnReceiver entityDeposit, EntityMapping entities
    ) {
        super(
                id, initialPosition, initialRotation, 0.5f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.7f, renderTimer, 0.3f, 0.5f, entityDeposit, entities
        );

        shape = GeneralShapes.CONCEPT_BLUEPRINT; // SCALE IS 0.5
        Pair<PosVector, Float> minimalCircle = shape.getMinimalCircle();
        shapeMiddle = minimalCircle.left;
        shapeRange = minimalCircle.right;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        ms.scale(-1, 1, 1);
        action.accept(shape);
        ms.popMatrix();
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public PosVector getPilotEyePosition() {
        return relativeInterpolatedDirection(new DirVector(3, 0, 1)).toPosVector();
    }

    @Override
    public float getRange() {
        return shapeRange;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return extraPosition.add(shapeMiddle, new PosVector());
    }

    public static class Factory extends EntityFactory {
        public Factory() {
            super();
        }

        public Factory(PosVector position, DirVector direction, DirVector velocity) {
            super(EntityClass.BASIC_JET, position, Toolbox.xTo(direction), velocity);
        }

        public Factory(BasicJet jet) {
            super(EntityClass.BASIC_JET, jet);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new BasicJet(id, position, rotation, game.getTimer(), game, entities);
        }
    }
}
