package nl.NG.Jetfightergame.FighterJets;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Structures.ShapeFromMesh;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public class PlayerJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 500f;
    public static final float BRAKE_POWER = 500f;
    public static final float MASS = 100f;
    public static final Material MATERIAL = Material.GOLD;
    public static final float YAW_POWER = 45f;
    public static final float PITCH_POWER = 45f;
    public static final float ROLL_POWER = 45f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.1f;

    private Shape shape;

    public PlayerJet(AbstractGameLoop engine, Controller input) {
        this(engine, PosVector.O, input, 0f);
    }

    public PlayerJet(AbstractGameLoop engine, PosVector initialPosition, Controller input, float initialRotation) {
        super(engine, input, initialPosition, initialRotation, 1f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                1f);
        shape = ShapeFromMesh.BASIC;
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void create(MatrixStack gl, Consumer<Shape> action, boolean takeStable) {
        gl.pushMatrix();
        gl.scale(-1, 1, 1);
        action.accept(shape);
        gl.popMatrix();
    }
}
