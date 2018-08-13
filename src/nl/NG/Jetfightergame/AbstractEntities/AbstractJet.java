package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupColor;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType;
import nl.NG.Jetfightergame.Assets.Entities.AbstractShield;
import nl.NG.Jetfightergame.Assets.Entities.OneHitShield;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.DeathIcosahedron;
import nl.NG.Jetfightergame.Assets.Entities.ReflectorShield;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType.*;
import static nl.NG.Jetfightergame.Settings.ServerSettings.INTERPOLATION_QUEUE_SIZE;

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

    protected Controller controller;
    protected Material surfaceMaterial;
    protected final EntityMapping entityMapping;
    protected List<BoosterLine> nuzzle;
    private DirVector forward;
    protected float baseThrust;

    /** time left in slow */
    protected float slowTimeLeft = 0;
    /** fraction of speed lost by slow. higher slow factor is more slow */
    protected float slowFactor = 0;
    /** time left in boost */
    private float boostTimeLeft = 0;
    /** fraction of speed gained by boost. higher boost factor is more speed */
    protected float boostFactor = 0;

    private VectorInterpolator forwardInterpolator;
    private VectorInterpolator velocityInterpolator;

    private PowerupType currentPowerup = PowerupType.NONE;

    /**
     * You are defining a complete Fighterjet here. good luck.
     * @param id                      unique identifier for this entity
     * @param initialPosition         position of spawning (of the origin) in world coordinates
     * @param initialRotation         the initial rotation of spawning
     * @param material                the default material properties of the whole object.
     * @param mass                    the mass of the object in kilograms.
     * @param liftFactor              arbitrary factor of the lift-effect of the wings in gravitational situations. This
     *                                is applied only on the vector of external influences, thus not in zero-gravity.
     * @param airResistanceCoeff      Air resistance coefficient Cw as in Fwl = 0.5 * A * Cw.
     * @param throttlePower           force of the engines at full power in Newton
     * @param brakePower              air resistance is multiplied with this value when braking
     * @param yawAcc                  acceleration over the Z-axis when moving right at full power in rad/ss
     * @param pitchAcc                acceleration over the Y-axis when pitching up at full power in rad/ss
     * @param rollAcc                 acceleration over the X-axis when rolling at full power in rad/ss
     * @param rotationReductionFactor the fraction that the rotationspeed is reduced every second [0, 1]
     * @param gameTimer               the timer that determines the "current rendering time" for {@link
     *                                MovingEntity#interpolatedPosition()}
     * @param yReduction              reduces drifting/stalling in horizontal direction by this fraction
     * @param zReduction              reduces drifting/stalling in vertical direction by this fraction
     * @param entityDeposit           the class that allows new entityMapping and particles to be added to the
     *                                environment
     * @param entityMapping           a mapping that allows binding id's to entityMapping
     */
    public AbstractJet(
            int id, PosVector initialPosition, Quaternionf initialRotation,
            Material material, float mass, float liftFactor, float airResistanceCoeff,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer gameTimer, float yReduction, float zReduction,
            SpawnReceiver entityDeposit, EntityMapping entityMapping
    ) {
        super(id, initialPosition, DirVector.zeroVector(), initialRotation, mass, gameTimer, entityDeposit);

        this.airResistCoeff = airResistanceCoeff;
        this.throttlePower = throttlePower;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
        this.liftFactor = liftFactor;
        this.rotationPreserveFactor = 1 - rotationReductionFactor;
        this.yPreservation = 1 - yReduction;
        this.zPreservation = 1 - zReduction;
        this.surfaceMaterial = material;
        this.entityMapping = entityMapping;
        this.controller = Controller.EMPTY;

        float time = gameTimer.time();
        forward = DirVector.xVector();
        relativeStateDirection(forward).normalize(forward);
        forwardInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, new DirVector(forward), time);
        velocityInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, DirVector.zeroVector(), time);
        baseThrust = ClientSettings.BASE_SPEED * ClientSettings.BASE_SPEED * airResistCoeff; // * c_w because we try to overcome air resist
        nuzzle = new ArrayList<>();

        Supplier<String> slowTimer = () -> {
            float factor = (1 - slowFactor) * boostFactor;
            if (factor == 0) return "";
            return String.format("Speed " + (factor > 1 ? "increased" : "reduced") + " to %d%% for %.1f seconds", ((int) (100 * factor)), Math.min(slowTimeLeft, boostTimeLeft));
        };
        Logger.printOnline(slowTimer);
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        controller.update();

        slowTimeLeft -= deltaTime;
        if (slowTimeLeft <= 0) {
            slowTimeLeft = 0;
            slowFactor = 0;
        }

        boostTimeLeft -= deltaTime;
        if (boostTimeLeft <= 0) {
            setBoost(0, 0);
        }

        gyroPhysics(deltaTime, netForce, velocity);

        if (currentPowerup != PowerupType.NONE && controller.primaryFire()) {
            usePowerup();
        }
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        if (other instanceof AbstractShield) return null;
        return super.checkCollisionWith(other, deltaTime);
    }

    private void setBoost(float duration, float factor) {
        boostTimeLeft = duration;
        boostFactor = factor;
    }

    public MovingEntity getTarget() {
        return getTarget(getForward(), getPosition(), entityMapping);
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
        float throttle = controller.throttle();
        float thrust = (throttle > 0) ? ((throttle * throttlePower) + baseThrust) : ((throttle + 1) * baseThrust);
        thrust = thrust > throttlePower ? throttlePower : thrust;
        thrust *= boostFactor + 1;
        netForce.add(forward.reducedTo(thrust, temp), netForce);

        float yPres = Toolbox.instantPreserveFraction(yPreservation, deltaTime);
        float zPres = Toolbox.instantPreserveFraction(zPreservation, deltaTime);

        // transform velocity to local, reduce drifting, then transform back to global space
        Quaternionf turnBack = rotation.invert(new Quaternionf());
        extraVelocity.rotate(turnBack);
        extraVelocity.mul(1f, yPres, zPres);
        extraVelocity.rotate(rotation);

        float rotationPreserveFraction = Toolbox.instantPreserveFraction(rotationPreserveFactor, deltaTime);
        yawSpeed *= rotationPreserveFraction;
        pitchSpeed *= rotationPreserveFraction;
        rollSpeed *= rotationPreserveFraction;

        // rotational forces
        float instYawAcc = yawAcc * deltaTime;
        float instPitchAcc = pitchAcc * deltaTime;
        float instRollAcc = rollAcc * deltaTime;
        yawSpeed += controller.yaw() * instYawAcc;
        pitchSpeed += controller.pitch() * instPitchAcc;
        rollSpeed += controller.roll() * instRollAcc;

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
     * @return forward in world-space (normalized, safe copy)
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
     * @param nOfBoosters total number of boosters
     * @param left        first relative position of booster
     * @param right       second relative position of booster
     */
    protected void addBooster(int nOfBoosters, PosVector left, PosVector right) {
        float pps = ClientSettings.THRUST_PARTICLES_PER_SECOND / nOfBoosters;
        nuzzle.add(new BoosterLine(
                left, right, DirVector.zeroVector(), pps, ClientSettings.THRUST_PARTICLE_LINGER_TIME,
                ClientSettings.THRUST_COLOR_1, ClientSettings.THRUST_COLOR_2, ClientSettings.THRUST_PARTICLE_SIZE
        ));
    }

    /**
     * @return current position of the pilot's eyes in world-space
     */
    public abstract PosVector getPilotEyePosition();

    /**
     * set the position of the jet
     * @see MovingEntity#set(PosVector, DirVector, Quaternionf, float)
     */
    public void set(PosVector posVector) {
        set(posVector, velocity, rotation, 0);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.ROUGH);

        DirVector back = getThrustDirection();
        float deltaTime = gameTimer.getRenderTime().difference();

        MatrixStack sm = new ShadowMatrix();
        for (BoosterLine boosterLine : nuzzle) {
            toLocalSpace(sm, () -> {
                entityDeposit.addParticles(
                        boosterLine.update(sm, back, deltaTime)
                );
            });
        }
    }

    public void set(EntityState spawn) {
        set(spawn.position(0), spawn.velocity(0), spawn.rotation(0), 0);
    }

    public void setController(Controller input) {
        this.controller = input;
    }

    /**
     * @return the current combination of powerups or null if the player has none
     */
    public PowerupType getCurrentPowerup() {
        return currentPowerup;
    }

    /**
     * adds one of the given type to the player's current powerup
     * @return true iff the powerup is accepted by the player
     */
    public boolean addPowerup(PowerupColor type) {
        PowerupType next = getCurrentPowerup().with(type);
        if (next == currentPowerup || next == NONE) return false;
        currentPowerup = next;
        return true;
    }

    public void setPowerup(PowerupType color) {
        currentPowerup = color;
    }

    private void usePowerup() {
        switch (currentPowerup) {
            case NONE:
                // honk
                break;
            case SPEED:
                setBoost(PowerupType.SPEED_BOOST_DURATION, PowerupType.SPEED_BOOST_FACTOR);
                break;
            case SHIELD:
                entityDeposit.addSpawn(new OneHitShield.Factory(this));
                break;
            case ROCKET:
                launchClusterRocket(this, getTarget(), entityDeposit);
                break;
            case SEEKERS:
                launchSeekers(this, entityDeposit, getTarget());
                break;
            case SMOKE:
                launchSmokeCloud(this, entityDeposit);
                break;
            case DEATHICOSAHEDRON:
                entityDeposit.addSpawn(new DeathIcosahedron.Factory(this));
                break;
            case REFLECTOR_SHIELD:
                entityDeposit.addSpawn(new ReflectorShield.Factory(this));
                break;
            case GRAPPLING_HOOK:
                PowerupType.launchGrapplingHook(this, getTarget(), entityDeposit);
                break;
            default:
                throw new UnsupportedOperationException("enum not registered properly: " + currentPowerup);
        }

        currentPowerup = PowerupType.NONE;
        entityDeposit.playerPowerupState(this, PowerupType.NONE);
    }

    private DirVector getThrustDirection() {
        float throttle = controller.throttle();
        float thrust = (throttle > 0) ? ((throttle * throttlePower) + baseThrust) : ((throttle + 1) * baseThrust);
        thrust += 0.2f;
        thrust *= boostFactor + 1;

        DirVector back = getForward().scale(ClientSettings.THRUST_PARTICLE_FACTOR * thrust);
        back.add(getVelocity());
        return back;
    }
}
