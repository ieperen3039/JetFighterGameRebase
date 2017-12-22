package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.EntityDefinitions.AbstractJet;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

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

    public TestJet(Controller input) {
        this(PosVector.zeroVector(), input, 1f, new Quaternionf());
    }

    public TestJet(PosVector initialPosition, Controller input, float scale, Quaternionf initialRotation) {
        super(input, initialPosition, initialRotation, scale,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT,
                THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                1f);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean extrapolate) {
        action.accept(GeneralShapes.CUBE);
    }

    @Override
    protected void updateShape(float deltaTime) {

    }
}
