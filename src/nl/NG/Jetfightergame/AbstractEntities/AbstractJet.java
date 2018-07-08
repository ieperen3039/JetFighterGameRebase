package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Assets.Weapons.MachineGun;
import nl.NG.Jetfightergame.Assets.Weapons.SpecialWeapon;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public abstract class AbstractJet extends MovingEntity {

    protected final float liftFactor;
    protected final float airResistCoeff;

    protected final float throttlePower;
    protected final float brakePower;
    protected final float yawAcc;
    protected final float pitchAcc;
    protected final float rollAcc;
    protected final float yPreservation;
    protected final float zPreservation;
    protected final float rotationPreserveFactor;

    protected final MachineGun gunAlpha;
    protected final SpecialWeapon gunBeta;

    protected Controller input;
    protected Material surfaceMaterial;
    private DirVector forward;

    /** time left in slow */
    protected float slowTimeLeft = 0;
    /** fraction of speed lost by slow. higher slow factor is more slow */
    protected float slowFactor = 0;

    private VectorInterpolator forwardInterpolator;
    private VectorInterpolator velocityInterpolator;

    public static final float BASE_SPEED = 200f;
    private float baseThrust;

    /**
     * You are defining a complete Fighterjet here. good luck.
     * @param id                       unique identifier for this entity
     * @param input                    controller input, either player or AI.
     * @param initialPosition          position of spawning (of the origin) in world coordinates
     * @param initialRotation          the initial rotation of spawning
     * @param scale                    scale factor applied to this object. the scale is in global space and executed in
     *                                 {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param material                 the default material properties of the whole object.
     * @param mass                     the mass of the object in kilograms.
     * @param liftFactor               arbitrary factor of the lift-effect of the wings in gravitational situations.
     *                                 This is applied only on the vector of external influences, thus not in
     *                                 zero-gravity.
     * @param airResistanceCoefficient 0.5 * A * Cw.
     * @param throttlePower            force of the engines at full power in Newton
     * @param brakePower               air resistance is multiplied with this value when braking
     * @param yawAcc                   acceleration over the Z-axis when moving right at full power in rad/ss
     * @param pitchAcc                 acceleration over the Y-axis when pitching up at full power in rad/ss
     * @param rollAcc                  acceleration over the X-axis when rolling at full power in rad/ss
     * @param rotationReductionFactor  the fraction that the rotationspeed is reduced every second [0, 1]
     * @param renderTimer              the timer that determines the "current rendering time" for {@link
     *                                 MovingEntity#interpolatedPosition()}
     * @param yReduction               reduces drifting/stalling in horizontal direction by this fraction
     * @param zReduction               reduces drifting/stalling in vertical direction by this fraction
     * @param gunAlpha                 the primary firing method
     * @param gunBeta                  the secondary gun. If this gun can be switched, this should be a GunManager.
     * @param entityDeposit            the class that allows new entities and particles to be added to the environment
     */
    public AbstractJet(
            int id, Controller input, PosVector initialPosition, Quaternionf initialRotation, float scale,
            Material material, float mass, float liftFactor, float airResistanceCoefficient,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer renderTimer, float yReduction, float zReduction,
            MachineGun gunAlpha, SpecialWeapon gunBeta, SpawnReceiver entityDeposit
    ) {
        super(id, initialPosition, DirVector.zeroVector(), initialRotation, mass, scale, renderTimer, entityDeposit);

        this.input = input;
        this.airResistCoeff = airResistanceCoefficient;
        this.throttlePower = throttlePower;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
        this.liftFactor = liftFactor;
        this.rotationPreserveFactor = 1 - rotationReductionFactor;
        this.yPreservation = 1 - yReduction;
        this.zPreservation = 1 - zReduction;
        this.gunAlpha = gunAlpha;
        this.gunBeta = gunBeta;
        this.surfaceMaterial = material;

        forward = DirVector.xVector();
        relativeStateDirection(forward).normalize(forward);
        forwardInterpolator = new VectorInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, new DirVector(forward));
        velocityInterpolator = new VectorInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, DirVector.zeroVector());
        baseThrust = BASE_SPEED * airResistCoeff; // * c_w because we try to overcome air resist

        Supplier<String> slowTimer = () -> slowTimeLeft > 0 ? String.format("%3d%% slow for %.1f seconds", ((int) (slowFactor * 100)), slowTimeLeft) : "";
        Logger.printOnline(slowTimer);
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        slowTimeLeft -= deltaTime;
        if (slowTimeLeft <= 0) {
            slowTimeLeft = 0;
            slowFactor = 0;
        }

        gyroPhysics(deltaTime, netForce, velocity);

        // in case of no guns
        if (gunAlpha == null) return;

        DirVector relativeGun = relativeStateDirection(new DirVector(5, 0, -1));

        final PosVector gunMount = position.add(relativeGun, new PosVector());
        final PosVector gunMount2 = extraPosition.add(relativeGun, new PosVector());

        State interpolator = new State(gunMount, gunMount2, rotation, extraRotation, velocity, forward);

        updateGun(deltaTime, interpolator, gunAlpha, input.primaryFire());
        updateGun(deltaTime, interpolator, gunBeta, input.secondaryFire());
    }

    /**
     * updates the firing of the gun
     */
    private void updateGun(float deltaTime, State interpolator, AbstractWeapon gunAlpha, boolean isFiring) {
        final Collection<Prentity> bullets;
        bullets = gunAlpha.update(deltaTime, isFiring, interpolator, entityDeposit);
        entityDeposit.addSpawns(bullets);
    }

    /**
     * physics model where input absolutely determines the plane rotation.
     * @param deltaTime timestamp in seconds
     * @param netForce  vector of force in N
     * @param velocity  movement vector with length in (m/s)
     */
    private void gyroPhysics(float deltaTime, DirVector netForce, DirVector velocity) {
        DirVector temp = new DirVector();

        // thrust forces
        float throttle = input.throttle();
        float thrust = (throttle > 0) ? ((throttle * throttlePower) + baseThrust) : ((throttle + 1) * baseThrust);
        thrust = thrust > throttlePower ? throttlePower : thrust;
        netForce.add(forward.reducedTo(thrust, temp), netForce);

        float yPres = instantPreserveFraction(yPreservation, deltaTime);
        float zPres = instantPreserveFraction(zPreservation, deltaTime);

        // transform velocity to local, reduce drifting, then transform back to global space
        Quaternionf turnBack = rotation.invert(new Quaternionf());
        extraVelocity.rotate(turnBack);
        extraVelocity.mul(1f, yPres, zPres);
        extraVelocity.rotate(rotation);

        float rotationPreserveFraction = instantPreserveFraction(rotationPreserveFactor, deltaTime);
        yawSpeed *= rotationPreserveFraction;
        pitchSpeed *= rotationPreserveFraction;
        rollSpeed *= rotationPreserveFraction;

        // rotational forces
        float instYawAcc = yawAcc * deltaTime;
        float instPitchAcc = pitchAcc * deltaTime;
        float instRollAcc = rollAcc * deltaTime;
        yawSpeed += input.yaw() * instYawAcc;
        pitchSpeed += input.pitch() * instPitchAcc;
        rollSpeed += input.roll() * instRollAcc;

        // air-resistance
        DirVector airResistance = new DirVector();
        float speedSq = velocity.lengthSquared();
        float brake = (throttle < 0) ? ((1 - throttle * brakePower) + 1) : 1;
        float resistance = airResistCoeff * brake;
        velocity.reducedTo(speedSq * resistance * -1, airResistance);
        airResistance.div(1 - slowFactor);
        netForce.add(airResistance);

        // F = m * a ; a = dv/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        extraVelocity.add(netForce.scale(deltaTime / mass, temp));

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, temp), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public void addStatePoint(float currentTime, PosVector newPosition, Quaternionf newRotation) {
        super.addStatePoint(currentTime, newPosition, newRotation);
        relativeStateDirection(DirVector.xVector()).normalize(forward);
        forwardInterpolator.add(new DirVector(forward), currentTime);
        velocityInterpolator.add(super.velocityAtRenderTime(), currentTime);
    }

    @Override
    public void impact(float power) {
        slowTimeLeft += power / (slowTimeLeft + 1);
        slowFactor = 0.5f; // TODO make it constant?
    }

    /**
     * @return forward in world-space (safe copy)
     */
    public DirVector getForward() {
        return new DirVector(forward);
    }

    public DirVector interpolatedForward() {
        forwardInterpolator.updateTime(renderTime());
        return forwardInterpolator.getInterpolated(renderTime()).toDirVector();
    }

    public DirVector interpolatedVelocity() {
        velocityInterpolator.updateTime(renderTime());
        return velocityInterpolator.getInterpolated(renderTime()).toDirVector();
    }

    /**
     * @return current position of the pilot's eyes in world-space
     */
    public abstract PosVector getPilotEyePosition();

    private static float instantPreserveFraction(float rotationPreserveFactor, float deltaTime) {
        return (float) (StrictMath.pow(rotationPreserveFactor, deltaTime));
    }

    /**
     * set the position of the jet
     * @see #set(PosVector, DirVector, Quaternionf)
     */
    public void set(PosVector posVector) {
        set(posVector, velocity, rotation);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }

    public void set(State spawn) {
        set(spawn.position(0), spawn.velocity(), spawn.rotation(0));
    }
}
