package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.EntityDefinitions.AbstractJet;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static java.lang.Math.toRadians;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public class PlayerJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 5000f;
    public static final float BRAKE_POWER = 500f;
    public static final float MASS = 1000f;
    public static final Material MATERIAL = Material.GOLD;
    public static final float YAW_POWER = (float) toRadians(10);
    public static final float PITCH_POWER = (float) toRadians(90);
    public static final float ROLL_POWER = (float) toRadians(140);
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.01f;

    private Shape shape;

    public PlayerJet(Controller input) {
        this(PosVector.zeroVector(), input, new Quaternionf());
    }

    public PlayerJet(PosVector initialPosition, Controller input, Quaternionf initialRotation) {
        super(input, initialPosition, initialRotation, 1f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.2f);
        shape = ShapeFromMesh.ConceptBlueprint;
    }

    @Override
    public void create(MatrixStack gl, Consumer<Shape> action, boolean extrapolate) {
        gl.pushMatrix();
        gl.scale(-1, 1, 1);
        action.accept(shape);
        gl.popMatrix();
    }

    @Override
    protected void updateShape(float deltaTime) {

    }
}
