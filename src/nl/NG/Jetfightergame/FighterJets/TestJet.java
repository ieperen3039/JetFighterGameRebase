package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.Structures.GeneralShapes;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 6-11-2017.
 */
public class TestJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float BRAKE_POWER = 1f;
    public static final float THROTTLE_POWER = 1f;
    public static final float MASS = 100f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 45f;
    public static final float PITCH_POWER = 45f;
    public static final float ROLL_POWER = 45f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.1f;

    public TestJet(AbstractGameLoop engine, Controller input) {
        this(engine, PosVector.O, input, 1f, 0f);
    }

    public TestJet(AbstractGameLoop engine, PosVector initialPosition, Controller input, float scale, float initialRotation) {
        super(engine, input, initialPosition, initialRotation, scale,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT,
                THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                1f);
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean takeStable) {
        action.accept(GeneralShapes.CUBE);
    }

}
