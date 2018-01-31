package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Rendering.Interpolation.QuaternionInterpolator;
import nl.NG.Jetfightergame.Rendering.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.DirVector;
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
    public static final float THROTTLE_POWER = 1000f;
    public static final float BRAKE_POWER = 200f;
    public static final float MASS = 100f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = (float) toRadians(10);
    public static final float PITCH_POWER = (float) toRadians(90);
    public static final float ROLL_POWER = (float) toRadians(140);
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.01f;

    private Shape shape;

    public PlayerJet(Controller input, GameTimer renderTimer) {
        this(PosVector.zeroVector(), input, new Quaternionf(), renderTimer);
    }

    public PlayerJet(PosVector initialPosition, Controller input, Quaternionf initialRotation, GameTimer renderTimer) {
        super(input, initialPosition, initialRotation, 1f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.1f, renderTimer, 1f, 1f);
        shape = ShapeFromMesh.CONCEPT_BLUEPRINT;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        ms.scale(-1, 1, 1);
        action.accept(shape);
        ms.popMatrix();
    }

    public void set(PosVector newPosition, DirVector newVelocity, Quaternionf newRotation){
        position.set(newPosition);
        velocity.set(newVelocity);

        positionInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, newPosition);
        rotationInterpolator = new QuaternionInterpolator(INTERPOLATION_QUEUE_SIZE, newRotation);
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public DirVector getPilotEyePosition() {
        return relativeInterpolatedDirection(new DirVector(3, 0, 1));
    }
}
