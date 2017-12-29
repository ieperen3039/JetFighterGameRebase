package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static java.lang.Math.toRadians;

/**
 * @author Geert van Ieperen
 *         created on 6-11-2017.
 */
public class TestJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 1000f;
    public static final float BRAKE_POWER = 200f;
    public static final float MASS = 100f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = (float) toRadians(10);
    public static final float PITCH_POWER = (float) toRadians(90);
    public static final float ROLL_POWER = (float) toRadians(140);
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.01f;

    public TestJet(Controller input, TrackedFloat renderTimer) {
        this(PosVector.zeroVector(), input, 1f, new Quaternionf(), renderTimer);
    }

    public TestJet(PosVector initialPosition, Controller input, float scale, Quaternionf initialRotation, TrackedFloat renderTimer) {
        super(input, initialPosition, initialRotation, scale,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT,
                THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                1f, renderTimer);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean extrapolate) {
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public DirVector getPilotEyePosition() {
        return DirVector.zeroVector();
    }
}
