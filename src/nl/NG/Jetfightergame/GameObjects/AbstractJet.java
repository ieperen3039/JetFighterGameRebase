package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Engine.Updatable;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import static nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothFloat.fractionOf;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public abstract class AbstractJet extends GameObject implements Updatable {

    protected final float mass;
    protected final float liftFactor;
    protected final float airResistCoeff;

    protected final float throttlePower;
    protected final float brakePower;
    protected final float yawAcc;
    protected final float pitchAcc;
    protected final float rollAcc;
    private final float rotationReductionFactor;

    protected Controller input;
    private DirVector forward;

    /**
     * oh boy. here we go
     * @param input controller input, either player or AI.
     * @param initialPosition position of spawning (of the origin) in world coordinates
     * @param initialRotation the initial rotation around the Z-axis of this object in radians
     * @param scale scale factor applied to this object. the scale is in global space and executed in {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param material the default material properties of the whole object.
     * @param mass the mass of the object in kilograms. this should refer to the weight of the base model in SpaceEngineers
     * @param liftFactor arbitrary factor of the lift-effect of the wings in gravitational situations.
*                   This is applied only on the vector of external influences, thus not in zero-gravity.
     * @param airResistanceCoefficient 0.5 * A * Cw. this is a factor that should be experimentally found
     * @param throttlePower force of the engines at full power in Newton
     * @param brakePower -
     * @param yawAcc acceleration over the Z-axis when moving right at full power in deg/s
     * @param pitchAcc acceleration over the Y-axis when moving up at full power in deg/s
     * @param rollAcc acceleration over the X-axis when rolling at full power in deg/s
     * @param rotationReductionFactor the fraction that the rotation is reduced every second
     */
    public AbstractJet(Controller input, PosVector initialPosition, float initialRotation, float scale,
                       Material material, float mass, float liftFactor, float airResistanceCoefficient,
                       float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
                       float rotationReductionFactor) {
        super(initialPosition, material, scale, initialRotation);

        this.mass = mass;
        this.input = input;
        this.airResistCoeff = airResistanceCoefficient;
        this.throttlePower = throttlePower;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
        this.liftFactor = liftFactor;
        this.rotationReductionFactor = rotationReductionFactor;
    }

    @Override
    public void applyPhisics(float deltaTime) {
        // obtain current x-axis in worldspace
        forward = getRelativeVector(DirVector.X, new ShadowMatrix()).toDirVector();

        netForce = DirVector.O;

        // thrust forces
        float throttle = input.throttle();
        float thrust = (throttle > 0 ? throttle * throttlePower : throttle * brakePower); // TODO use air-resistance to increase braking power
        netForce = netForce.add(forward.reduceTo(thrust));

        // rotational forces
        float yawMoment = (input.yaw() * yawAcc * deltaTime);
        rotationAxis = rotationAxis.rotateVector(DirVector.Z, yawMoment).toDirVector();
        float pitchMoment = (input.pitch() * pitchAcc * deltaTime);
        rotationAxis = rotationAxis.rotateVector(DirVector.Y, pitchMoment).toDirVector();
        float rollMoment = (input.roll() * rollAcc * deltaTime);
        rotationAxis = rotationAxis.rotateVector(DirVector.X, rollMoment).toDirVector();


        rotationSpeed = fractionOf(rotationSpeed, 0f, 1 - rotationReductionFactor, deltaTime);

        // air-resistance TODO this... make air resistance not overflow
        netForce = netForce.add(movement.reduceTo(movement.length() * movement.length() * airResistCoeff * -1));
        // apply phisics
        movement = movement.add(netForce.scale(deltaTime).scale(1/mass)); //TODO get this right

        update(deltaTime);
    }

    public DirVector getForward() {
        return forward;
    }

    @Override
    public void postUpdate() {
        super.postUpdate();
        position.update(extraPosition);
        rotation.update(extraRotation);
    }

    @Override
    public String toString(){
        return "Jet '" + this.getClass().getSimpleName() + "' {" +
                "pos: " + position.current() +
                ", move: " + movement +
                "}";
    }
}
