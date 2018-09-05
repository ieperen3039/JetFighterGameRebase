package nl.NG.Jetfightergame.Assets.Entities.FighterJets;

import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Sound.MovingAudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 11-8-2018.
 */
public class JetNightHawk extends AbstractJet {
    public static final float THROTTLE_POWER = 1400f;
    public static final float BRAKE_POWER = 2f; // air resist is multiplied with this
    public static final float MASS = 80f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 1f;
    public static final float PITCH_POWER = 2f;
    public static final float ROLL_POWER = 3f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.2f;
    public static final float ROTATION_REDUCTION_FACTOR = 0.7f;
    public static final float Y_REDUCTION = 0.2f;
    public static final float Z_REDUCTION = 0.8f;

    private final PosVector shapeMiddle;
    private final float shapeRange;

    private JetNightHawk(
            int id, PosVector initialPosition, Quaternionf initialRotation, GameTimer renderTimer,
            SpawnReceiver entityDeposit, EntityMapping entities, Color4f color
    ) {
        super(
                id, initialPosition, initialRotation,
                MATERIAL, MASS, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                ROTATION_REDUCTION_FACTOR, renderTimer, Y_REDUCTION, Z_REDUCTION, entityDeposit, entities, color
        );

        Pair<PosVector, Float> minimalCircle = CustomJetShapes.NIGHT_HAWK.getMinimalCircle();
        shapeMiddle = minimalCircle.left;
        shapeRange = minimalCircle.right;

        PairList<PosVector, PosVector> boosters = CustomJetShapes.nightHawkBoosters;
        for (int i = 0; i < boosters.size(); i++) {
            addBooster(boosters.size(), boosters.left(i), boosters.right(i));
        }
    }

    @Override
    public PosVector getPilotEyePosition() {
        return new PosVector(3.5f, 0, 1);
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        action.accept(CustomJetShapes.NIGHT_HAWK);
    }

    @Override
    public float getRange() {
        return shapeRange;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return extraPosition.add(shapeMiddle, new PosVector());
    }

    @Override
    protected MovingAudioSource getBoosterSound() {
        return new MovingAudioSource(Sounds.booster, this, 0.01f, BOOSTER_GAIN, true);
    }

    public static class Factory extends JetFactory {

        public Factory() {
            super();
        }

        public Factory(PosVector position, DirVector direction, DirVector velocity, Color4f color) {
            super(EntityClass.JET_NIGHT_HAWK, position, Toolbox.xTo(direction), velocity, color);
        }

        public Factory(JetNightHawk jet) {
            super(EntityClass.JET_NIGHT_HAWK, jet);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new JetNightHawk(id, position, rotation, game.getTimer(), game, entities, color);
        }
    }
}
