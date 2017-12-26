package nl.NG.Jetfightergame.EntityDefinitions;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
     * @param rotationReductionFactor the fraction that the rotationspeed is reduced every second [0, 1]
     * @param renderTimer the timer that determines the "current rendering time" for {@link MovingEntity#interpolatedPosition()}
     */
    public AbstractJet(Controller input, PosVector initialPosition, Quaternionf initialRotation, float scale,
                       Material material, float mass, float liftFactor, float airResistanceCoefficient,
                       float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
                       float rotationReductionFactor, TrackedFloat renderTimer) {
        super(material, mass, scale, initialPosition, DirVector.zeroVector(), initialRotation, renderTimer);

        this.input = input;
        this.airResistCoeff = airResistanceCoefficient;
        this.throttlePower = throttlePower;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
        this.liftFactor = liftFactor;
        this.rotationReductionFactor = rotationReductionFactor;
        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    @Override
    public void applyPhysics(float deltaTime, DirVector netForce) {
        // nothing to do when no time is passed
        if (deltaTime == 0) {
            extraRotation.set(rotation);
            extraPosition.set(position);
            return;
        }

        if (Settings.GYRO_PHYSICS_MODEL){
            gyroPhysics(deltaTime, netForce, velocity);
        } else {
            forwardVelocityPhysics(deltaTime, velocity.length(), netForce.dot(forward));
        }
    }

    /**
     * A simple model where the rotation of the plane determins the movement of it.
     * @param deltaTime timestamp in seconds
     * @param speed movement of this plane (m/s)
     * @param forwardForce
     */
    private void forwardVelocityPhysics(float deltaTime, float speed, float forwardForce) {

        float airResistance = speed * speed * airResistCoeff;

        // thrust forces
        float throttle = input.throttle();
        float thrust = (throttle > 0 ? throttle * throttlePower : throttle * brakePower * airResistance);
        forwardForce += thrust;

        // exponential reduction of speed (before rotational forces, as this is the result of momentum)
        float preserveFraction = (float) (1 - StrictMath.pow(rotationReductionFactor, deltaTime));
        yawSpeed *= preserveFraction;
        pitchSpeed *= preserveFraction;
        rollSpeed *= preserveFraction;

        // rotational forces
        float instYawAcc = (float) StrictMath.pow(yawAcc, deltaTime);
        float instPitchAcc = (float) StrictMath.pow(pitchAcc, deltaTime);
        float instRollAcc = (float) StrictMath.pow(rollAcc, deltaTime);
        yawSpeed += input.yaw() * instYawAcc;
        pitchSpeed += input.pitch() * instPitchAcc;
        rollSpeed += input.roll() * instRollAcc;

        // air-resistance
        forwardForce -= airResistance * deltaTime;

        // F = m * a ; a = dv/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        speed += forwardForce * (deltaTime/mass);

        // collect extrapolated variables
        position.add(forward.reducedTo(speed * deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    /**
     * physics model where input absolutely determines the plane rotation, and force is applied directional
     * @param deltaTime timestamp in seconds
     * @param netForce vector of force in N
     * @param velocity movement vector with length in (m/s)
     */
    private void gyroPhysics(float deltaTime, DirVector netForce, DirVector velocity) {

        // thrust forces
        float throttle = input.throttle();
        float thrust = (throttle > 0 ? throttle * throttlePower : throttle * brakePower);
        netForce.add(forward.reducedTo(thrust, new DirVector()), netForce);

        // exponential reduction of speed (before rotational forces, as this is the result of momentum)
        float preserveFraction = (float) (1 - StrictMath.pow(rotationReductionFactor, deltaTime));
        yawSpeed *= preserveFraction;
        pitchSpeed *= preserveFraction;
        rollSpeed *= preserveFraction;

        // rotational forces
        float instYawAcc = (float) StrictMath.pow(yawAcc, deltaTime);
        float instPitchAcc = (float) StrictMath.pow(pitchAcc, deltaTime);
        float instRollAcc = (float) StrictMath.pow(rollAcc, deltaTime);
        yawSpeed += input.yaw() * instYawAcc;
        pitchSpeed += input.pitch() * instPitchAcc;
        rollSpeed += input.roll() * instRollAcc;

        // air-resistance
        DirVector airResistance = new DirVector();
        float speed = velocity.length();
        velocity.reducedTo(speed * speed * airResistCoeff * -1, airResistance);
        velocity.add(airResistance.scale(deltaTime, airResistance));

        // F = m * a ; a = dv/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        DirVector extraVelocity = new DirVector();
        netForce.scale(deltaTime/mass, extraVelocity).add(velocity, extraVelocity);

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    public void update(float currentTime, float deltaTime) {
        super.update(currentTime, deltaTime);
        // obtain current x-axis in worldspace
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    @Override
    public void applyCollision() {
        // relative position of contact
        PosVector relativeHit = nextCrash.get().hitPos;
        // normal of the plane of contact
        DirVector contactNormal = nextCrash.get().normal;
        // current rotation speed of airplane
        Vector3f rotationSpeedVector = new Vector3f(rollSpeed, pitchSpeed, yawSpeed);



    }

    /**
     * @return forward in world-space
     */
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

    /**
     * @return current position of the pilot's eyes in world-space
     */
    public abstract DirVector getPilotEyePosition();
}
