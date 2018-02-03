package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public class BasicJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 1000f;
    public static final float BRAKE_POWER = 200f;
    public static final float MASS = 50f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 1f;
    public static final float PITCH_POWER = 2f;
    public static final float ROLL_POWER = 2f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.01f;

    private Shape shape;

    public BasicJet(Controller input, GameTimer renderTimer) {
        this(PosVector.zeroVector(), input, new Quaternionf(), renderTimer);
    }

    public BasicJet(PosVector initialPosition, Controller input, Quaternionf initialRotation, GameTimer renderTimer) {
        super(input, initialPosition, initialRotation, 1f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.8f, renderTimer, 1f, 1f, INTERPOLATION_QUEUE_SIZE);
        shape = ShapeFromMesh.CONCEPT_BLUEPRINT;
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
    public DirVector getPilotEyePosition() {
        return relativeInterpolatedDirection(new DirVector(3, 0, 1));
    }

}
