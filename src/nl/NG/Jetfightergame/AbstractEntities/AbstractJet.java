package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Assets.Sounds;
import nl.NG.Jetfightergame.Assets.Weapons.AbstractWeapon;
import nl.NG.Jetfightergame.Assets.Weapons.MachineGun;
import nl.NG.Jetfightergame.Assets.Weapons.SpecialWeapon;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Primitives.Particles.FireParticle;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Primitives.Particles.Particles;
import nl.NG.Jetfightergame.Rendering.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public abstract class AbstractJet extends GameEntity implements MortalEntity {

    /**
     * arbitrary number. higher == more boom
     */
    private static final float EXPLOSION_POWER = 10;

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

    /**
     * lose it all, and you're dead
     */
    protected int hitPoints;
    /**
     * the number of hitpoints cannot exceed this number
     */
    private final int maxHeath;

    protected transient Controller input;
    private DirVector forward;

    private VectorInterpolator forwardInterpolator;
    private VectorInterpolator velocityInterpolator;

    private static final float BASE_SPEED = 1000f;
    private final float defaultThrustSquared;

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
     * @param airResistanceCoefficient 0.5 * A * Cw. This is a factor that should be experimentally found
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
     * @param hitPoints                the amount of damage this plane can take before exploding
     * @param entityDeposit            the class that allows new entities and particles to be added to the environment
     */
    public AbstractJet(
            int id, Controller input, PosVector initialPosition, Quaternionf initialRotation, float scale,
            Material material, float mass, float liftFactor, float airResistanceCoefficient,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer renderTimer, float yReduction, float zReduction,
            MachineGun gunAlpha, SpecialWeapon gunBeta, int hitPoints, SpawnReceiver entityDeposit
    ) {
        super(id, initialPosition, DirVector.zeroVector(), initialRotation, material, mass, scale, renderTimer, entityDeposit);

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
        this.hitPoints = hitPoints;
        this.maxHeath = hitPoints;

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
        forwardInterpolator = new VectorInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, new DirVector(forward));
        velocityInterpolator = new VectorInterpolator(ServerSettings.INTERPOLATION_QUEUE_SIZE, DirVector.zeroVector());
        defaultThrustSquared = BASE_SPEED * BASE_SPEED * airResistCoeff; // * c_w because we try to overcome air resist
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        gyroPhysics(deltaTime, netForce, velocity);

        final PosVector gunMount = new PosVector(position);
        gunMount.add(relativeStateDirection(new DirVector(10, 0, -2)));
        final PosVector gunMount2 = new PosVector(extraPosition);
        gunMount2.add(relativeStateDirection(new DirVector(10, 0, -2)));

        State interpolator = new State(gunMount, gunMount2, rotation, extraRotation, velocity, forward);

        updateGun(deltaTime, interpolator, gunAlpha, input.primaryFire());
        updateGun(deltaTime, interpolator, gunBeta, input.secondaryFire());
    }

    /**
     * updates the firing of the gun
     */
    private void updateGun(float deltaTime, State interpolator, AbstractWeapon gunAlpha, boolean isFiring) {
        final Collection<Spawn> bullets;
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
        final float baseThrust = defaultThrustSquared * airResistCoeff;
        float thrust = (throttle > 0) ? ((throttle * throttlePower) + baseThrust) : ((throttle + 1) * baseThrust);
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
        float speed = velocity.length();
        float brake = (throttle < 0) ? (-throttle * brakePower) : 1;
        velocity.reducedTo(speed * speed * (airResistCoeff * brake) * -1, airResistance);
        extraVelocity.add(airResistance.scale(deltaTime, temp));

        // F = m * a ; a = dv/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        extraVelocity.add(netForce.scale(deltaTime / mass, temp), extraVelocity);

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, temp), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    public void update(float currentTime) {
        super.update(currentTime);

        // obtain current x-axis in worldspace
        relativeStateDirection(DirVector.xVector()).normalize(forward);
        forwardInterpolator.add(new DirVector(forward), currentTime);
        velocityInterpolator.add(new DirVector(velocity), currentTime);
    }

    @Override
    public void impact(PosVector impact, float power) {
        hitPoints -= power + 1;
        if (isDead()) entityDeposit.addParticles(this.explode());
    }

    public boolean isDead() {
        return hitPoints <= 0;
    }

    /**
     * @return forward in world-space
     */
    public DirVector getForward() {
        return forward;
    }

    public DirVector interpolatedForward() {
        return forwardInterpolator.getInterpolated(renderTime()).toDirVector();
    }

    public DirVector interpolatedVelocity() {
        return velocityInterpolator.getInterpolated(renderTime()).toDirVector();
    }

    /**
     * set the state of this plane to the given parameters. This also updates the interpolation cache, which may result
     * in temporal visual glitches. Usage is preferably restricted to switching worlds
     */
    public void set(PosVector newPosition, DirVector newVelocity, Quaternionf newRotation) {
        this.position = new PosVector(newPosition);
        this.extraPosition = new PosVector(newPosition);
        this.rotation = new Quaternionf(newRotation);
        this.extraRotation = new Quaternionf(newRotation);
        this.velocity = new DirVector(newVelocity);
        this.extraVelocity = new DirVector(newVelocity);

        yawSpeed = 0f;
        pitchSpeed = 0f;
        rollSpeed = 0f;

        hitPoints = maxHeath;
        resetCache();
    }

    /**
     * @return current position of the pilot's eyes in world-space
     */
    public abstract DirVector getPilotEyePosition();

    /**
     * #BOOM This method does not remove this entity, only generate particles. It does however set the number of
     * hitpoints to 0, so it will be scheduled for removal, if necessary.
     * @return the generated particles resulting from this entity
     */
    public Collection<Particle> explode() {
        hitPoints = 0;

        float force = EXPLOSION_POWER;
        Collection<Particle> result = new ArrayList<>();
        ShadowMatrix sm = new ShadowMatrix();
        Toolbox.print(getVelocity());

        Consumer<Shape> particleMapper = (shape) -> shape.getPlaneStream()
//                .parallel()
                .map(p -> Particles.splitIntoParticles(p, sm, this.getPosition(), force, Color4f.GREY, getVelocity()))
                .forEach(result::addAll);

        toLocalSpace(sm, () -> create(sm, particleMapper));

        for (int i = 0; i < ClientSettings.FIRE_PARTICLE_DENSITY; i++) {
            result.add(FireParticle.randomParticle(getPosition(), force * 2, 2));
        }

        new AudioSource(Sounds.explosion, getPosition(), 1f, 1f);

        return result;
    }

    private static float instantPreserveFraction(float rotationPreserveFactor, float deltaTime) {
        return (float) (StrictMath.pow(rotationPreserveFactor, deltaTime));
    }

    /**
     * sets this jet to the middle of the world
     */
    public void set() {
        set(PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf());
    }

    /**
     * set the position of the jet
     * @see #set(PosVector, DirVector, Quaternionf)
     */
    public void set(PosVector posVector) {
        set(posVector, velocity, rotation);
    }
}
