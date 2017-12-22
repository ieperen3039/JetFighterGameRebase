package nl.NG.Jetfightergame.EntityDefinitions;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public abstract class AbstractJet extends GameEntity {

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
     * You are defining a complete Fighterjet here. good luck.
     * @param input controller input, either player or AI.
     * @param initialPosition position of spawning (of the origin) in world coordinates
     * @param initialRotation the initial rotation around the Z-axis of this object in radians
     * @param scale scale factor applied to this object. the scale is in global space and executed in {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param material the default material properties of the whole object.
     * @param mass the mass of the object in kilograms. this should refer to the weight of the base model in SpaceEngineers
     *             multiplied by {@code scale}^3
     * @param liftFactor arbitrary factor of the lift-effect of the wings in gravitational situations.
*                   This is applied only on the vector of external influences, thus not in zero-gravity.
     * @param airResistanceCoefficient 0.5 * A * Cw. this is a factor that should be experimentally found
     * @param throttlePower force of the engines at full power in Newton
     * @param brakePower (not yet determined)
     * @param yawAcc acceleration over the Z-axis when moving right at full power in rad/s
     * @param pitchAcc acceleration over the Y-axis when pitching up at full power in rad/s
     * @param rollAcc acceleration over the X-axis when rolling at full power in rad/s
     * @param rotationReductionFactor the fraction that the rotation is reduced every second
     */
    public AbstractJet(Controller input, PosVector initialPosition, float initialRotation, float scale,
                       Material material, float mass, float liftFactor, float airResistanceCoefficient,
                       float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
                       float rotationReductionFactor) {
        super(initialPosition, material, scale, initialRotation, mass);

        this.input = input;
        this.airResistCoeff = airResistanceCoefficient;
        this.throttlePower = throttlePower;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
        this.liftFactor = liftFactor;
        this.rotationReductionFactor = rotationReductionFactor;
        forward = relativeToWorldSpace(DirVector.xVector(), new ShadowMatrix()).toDirVector();
    }

    @Override
    public void applyPhysics(float deltaTime, DirVector netForce) {
        // thrust forces
        float throttle = input.throttle();
        float thrust = (throttle > 0 ? throttle * throttlePower : throttle * brakePower);
        netForce.add(forward.reduceTo(thrust, new DirVector()), netForce);

        // rotational forces TODO correct rotation of airplane

        // air-resistance
        netForce.add(velocity.reduceTo(velocity.length() * velocity.length() * airResistCoeff * -1, new DirVector()), netForce);


        // collect extrapolated variables
        // F = m * a ; a = dv/dt ; v = ds/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        velocity = netForce.scale(deltaTime/mass, new DirVector());

        // ds = v * dt ;
        extraPosition = position.add(velocity.scale(deltaTime, new DirVector()), new PosVector());
        extraRotation += ExponentialSmoothFloat.fractionOf(rotationSpeed, 0f, 1 - rotationReductionFactor, deltaTime);
    }

    public void update(float currentTime, float deltaTime) {
        super.update(currentTime, deltaTime);
        // obtain current x-axis in worldspace
        forward = relativeToWorldSpace(DirVector.xVector(), new ShadowMatrix()).toDirVector();
    }

    @Override
    public void applyCollision() {
        //TODO elastic collision of rigid bodies
    }

    public DirVector getForward() {
        return forward;
    }

    @Override
    public String toString(){
        return "Jet '" + this.getClass().getSimpleName() + "' {" +
                "pos: " + position +
                ", velocity: " + getVelocity() +
                ", direction: " + getForward() +
                "}";
    }
}
