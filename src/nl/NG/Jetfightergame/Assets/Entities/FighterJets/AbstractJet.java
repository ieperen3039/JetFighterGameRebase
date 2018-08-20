package nl.NG.Jetfightergame.Assets.Entities.FighterJets;

import nl.NG.Jetfightergame.Assets.Entities.*;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType.*;
import static nl.NG.Jetfightergame.Settings.ClientSettings.*;
import static nl.NG.Jetfightergame.Settings.ServerSettings.GENERAL_SPEED_FACTOR;
import static nl.NG.Jetfightergame.Settings.ServerSettings.INTERPOLATION_QUEUE_SIZE;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public abstract class AbstractJet extends MovingEntity {
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
    private List<BoosterLine> nuzzle;
    private DirVector forward;

    /** left = factor , right = duration */
    private Collection<Pair<Float, Float>> speedModifiers;

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
            Material material, float mass, float airResistanceCoeff,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer gameTimer, float yReduction, float zReduction,
            SpawnReceiver entityDeposit, EntityMapping entityMapping
    ) {
        super(id, initialPosition, DirVector.zeroVector(), initialRotation, mass, gameTimer, entityDeposit);

        this.airResistCoeff = airResistanceCoeff / GENERAL_SPEED_FACTOR;
        this.throttlePower = throttlePower * GENERAL_SPEED_FACTOR;
        this.brakePower = brakePower;
        this.yawAcc = yawAcc;
        this.pitchAcc = pitchAcc;
        this.rollAcc = rollAcc;
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
        nuzzle = new ArrayList<>();
        speedModifiers = new HashSet<>();
    }

    @Override
    public void applyPhysics(DirVector netForce) {
        controller.update();

        float time = gameTimer.time();
        float deltaTime = gameTimer.getGameTime().difference();
        speedModifiers.removeIf(p -> p.right < time);

        gyroPhysics(deltaTime, netForce, velocity);

        relativeStateDirection(DirVector.xVector()).normalize(forward);
        if (currentPowerup != PowerupType.NONE && controller.primaryFire()) {
            usePowerup();
        }
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        if (other instanceof AbstractShield) return null;
        return super.checkCollisionWith(other, deltaTime);
    }

    public void addSpeedModifier(float factor, float duration) {
        speedModifiers.add(new Pair<>(factor, gameTimer.time() + duration));
    }

    public MovingEntity getTarget() {
        return getTarget(getForward(), getPosition(), entityMapping);
    }

    /**
     * physics model where input deterministically determines the plane rotation.
     * @param deltaTime timestamp in seconds
     * @param netForce  vector of force in N
     * @param velocity  movement vector with length in (m/s)
     */
    private void gyroPhysics(float deltaTime, DirVector netForce, DirVector velocity) {
        DirVector temp = new DirVector();

        // thrust forces
        float throttle = controller.throttle();
        float thrust = (throttle > 0) ? ((throttle * throttlePower) + (float) 0) : ((throttle + 1) * (float) 0);
        thrust = thrust > throttlePower ? throttlePower : thrust;

        // apply speed modifiers
        for (Pair<Float, Float> modifier : speedModifiers) {
            thrust *= modifier.left;
        }

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

        // apply speed modifiers
        for (Pair<Float, Float> modifier : speedModifiers) {
            resistance /= modifier.left;
        }

        velocity.reducedTo(speedSq * resistance * -1, airResistance);
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

        velocity.set(super.velocityAtRenderTime());
        velocityInterpolator.add(new DirVector(velocity), currentTime);
    }

    @Override
    public void impact(float factor, float duration) {
        addSpeedModifier(1f / factor, duration);
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
        float pps = THRUST_PARTICLES_PER_SECOND / nOfBoosters;
        nuzzle.add(new BoosterLine(
                left, right, DirVector.zeroVector(), pps, THRUST_PARTICLE_LINGER_TIME,
                THRUST_COLOR_1, THRUST_COLOR_2, THRUST_PARTICLE_SIZE, gameTimer
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

        DirVector trail = getForward();
        trail.mul(-JET_THRUST_SPEED).add(getVelocity());

        float thrust = 0.5f * (controller.throttle() + 1.0f);
        // apply trust modifiers
        for (Pair<Float, Float> modifier : speedModifiers) {
            thrust *= modifier.left;
        }
        float pps = Math.max((THRUST_PARTICLES_PER_SECOND * thrust * thrust) / nuzzle.size(), 3);

        PosVector currPos = interpolatedPosition();
        Quaternionf currRot = interpolatedRotation();

        MatrixStack sm = new ShadowMatrix();
        toLocalSpace(sm, () -> nuzzle.forEach(boosterLine ->
                entityDeposit.addParticles(boosterLine.update(sm, trail, 0.1f, pps))), currPos, currRot
        );

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
            case SPEED_BOOST:
                addSpeedModifier(PowerupType.SPEED_BOOST_FACTOR, PowerupType.SPEED_BOOST_DURATION);
                entityDeposit.boosterColorChange(this, Color4f.YELLOW, Color4f.WHITE, SPEED_BOOST_DURATION);
                break;
            case SHIELD:
                entityDeposit.addSpawn(new OneHitShield.Factory(this));
                break;
            case ROCKET:
                launchClusterRocket(this, getTarget(), entityDeposit);
                break;
            case SEEKERS:
                launchSeekers(this, entityDeposit, this::getTarget);
                break;
            case BLACK_HOLE:
                entityDeposit.addSpawn(new BlackHole.Factory(this));
                break;
            case SMOKE:
                launchSmokeCloud(this, entityDeposit);
                break;
            case DEATHICOSAHEDRON:
                entityDeposit.addSpawn(new DeathIcosahedron.Factory(this));
                break;
            case STAR_BOOST:
                doStarBoost(this, this.entityDeposit);
                break;
            case REFLECTOR_SHIELD:
                entityDeposit.addSpawn(new ReflectorShield.Factory(this));
                break;
            case GRAPPLING_HOOK:
                entityDeposit.addSpawn(new GrapplingHook.Factory(this, getTarget()));
                break;
            default:
                throw new UnsupportedOperationException("powerup not properly registered: " + currentPowerup);
        }

        currentPowerup = PowerupType.NONE;
        entityDeposit.playerPowerupState(this, PowerupType.NONE);
    }

    public void setBoosterColor(Color4f color1, Color4f color2, float duration) {
        nuzzle.forEach(n -> n.setColor(color1, color2, duration));
    }

    private MovingEntity getTarget(EntityState s) {
        return getTarget(s.velocity(0), s.position(0), entityMapping);
    }

    public String getPlaneDataString() {
        float factor = 1f;
        for (Pair<Float, Float> modifier : speedModifiers) {
            factor *= modifier.left;
        }

        String boost = (factor == 1f) ? "" : String.format(
                "Speed " + (factor > 1 ? "increased" : "reduced") + " to %d%%",
                ((int) (100 * factor))
        );

        return String.format("[ %s ] %s", toString(), boost);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
