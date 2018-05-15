package nl.NG.Jetfightergame.Assets.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Assets.Weapons.MachineGun;
import nl.NG.Jetfightergame.Assets.Weapons.SpecialWeapon;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromFile;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public class BasicJet extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 1000f;
    public static final float BRAKE_POWER = 3f; // air resist is multiplied with this
    public static final float MASS = 50f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 1f;
    public static final float PITCH_POWER = 2f;
    public static final float ROLL_POWER = 2f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.002f;
    private final float range = (float) Math.sqrt(3 * (6f * scale) * (6f * scale));
    private static final MachineGun GUN = new MachineGun(0.1f);

    private final Shape shape;

    public BasicJet(int id, Controller input, GameTimer renderTimer, SpawnReceiver entityDeposit) {
        this(id, PosVector.zeroVector(), input, new Quaternionf(), renderTimer, entityDeposit, new SpecialWeapon(5));
    }

    public BasicJet(int id, PosVector initialPosition, Controller input, Quaternionf initialRotation, GameTimer renderTimer,
                    SpawnReceiver entityDeposit, SpecialWeapon specialWeapon) {
        super(id, input, initialPosition, initialRotation, 1f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.8f, renderTimer, 0.3f, 0.3f, GUN, specialWeapon, 1000, entityDeposit);

        shape = ShapeFromFile.CONCEPT_BLUEPRINT;

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

    @Override
    public float getRange() {
        return range;
    }
}
