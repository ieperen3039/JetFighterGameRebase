package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupColor;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.Seeker;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType.*;
import static nl.NG.Jetfightergame.Settings.ServerSettings.INTERPOLATION_QUEUE_SIZE;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public abstract class AbstractJet extends MovingEntity {

    private static final int SMOKE_DISTRACTION_ELEMENTS = 3;
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
    private DirVector forward;
    private float baseThrust;

    /** time left in slow */
    protected float slowTimeLeft = 0;
    /** fraction of speed lost by slow. higher slow factor is more slow */
    protected float slowFactor = 0;
    /** time left in boost */
    private float boostDuration = 0;
    /** fraction of speed gained by boost. higher boost factor is more speed */
    private float boostFactor = 0;

    private VectorInterpolator forwardInterpolator;
    private VectorInterpolator velocityInterpolator;

    private PowerupType currentPowerup = PowerupType.NONE;

    /**
     * You are defining a complete Fighterjet here. good luck.
     * @param id                       unique identifier for this entity
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
     * @param gameTimer                the timer that determines the "current rendering time" for {@link
     *                                 MovingEntity#interpolatedPosition()}
     * @param yReduction               reduces drifting/stalling in horizontal direction by this fraction
     * @param zReduction               reduces drifting/stalling in vertical direction by this fraction
     * @param entityDeposit            the class that allows new entityMapping and particles to be added to the environment
     * @param entityMapping                 a mapping that allows binding id's to entityMapping
     */
    public AbstractJet(
            int id, PosVector initialPosition, Quaternionf initialRotation, float scale,
            Material material, float mass, float liftFactor, float airResistanceCoefficient,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer gameTimer, float yReduction, float zReduction,
            SpawnReceiver entityDeposit, EntityMapping entityMapping
    ) {
        super(id, initialPosition, DirVector.zeroVector(), initialRotation, mass, scale, gameTimer, entityDeposit);

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
        this.surfaceMaterial = material;
        this.entityMapping = entityMapping;
        this.controller = Controller.EMPTY;

        float time = gameTimer.time();
        forward = DirVector.xVector();
        relativeStateDirection(forward).normalize(forward);
        forwardInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, new DirVector(forward), time);
        velocityInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, DirVector.zeroVector(), time);
        baseThrust = ClientSettings.BASE_SPEED * ClientSettings.BASE_SPEED * airResistCoeff; // * c_w because we try to overcome air resist

        Supplier<String> slowTimer = () -> slowTimeLeft > 0 ? String.format("%3d%% slow for %.1f seconds", ((int) (slowFactor * 100)), slowTimeLeft) : "";
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

        boostDuration -= deltaTime;
        if (boostDuration <= 0) {
            boostDuration = 0;
            boostFactor = 0;
        }

        gyroPhysics(deltaTime, netForce, velocity);

        if (currentPowerup != PowerupType.NONE && controller.primaryFire()) {
            usePowerup(currentPowerup);

            currentPowerup = PowerupType.NONE;
            entityDeposit.playerPowerupState(this, PowerupType.NONE);
        }
    }

    private void usePowerup(PowerupType type) {
        switch (type) {
            case SPEED:
                boostDuration = PowerupType.SPEED_BOOST_DURATION;
                boostFactor = PowerupType.SPEED_BOOST_FACTOR;
                break;
            case SHIELD:
                break;
            case ROCKET:
                launchSeekers();
                break;
            case SMOKE:
                launchSmokeCloud();
                break;
        }
    }

    private void launchSmokeCloud() {
        DirVector dir = getForward();
        dir.scale(-SMOKE_LAUNCH_SPEED).add(velocity.scale(0.5f, new DirVector()));
        entityDeposit.addExplosion(
                position, dir,
                Color4f.BLACK, Color4f.GREY,
                SMOKE_SPREAD, SMOKE_DENSITY, SMOKE_LINGER_TIME, 10f
        );
        // distraction
        for (int i = 0; i < SMOKE_DISTRACTION_ELEMENTS; i++) {
            DirVector move = new DirVector(dir);
            move.add(DirVector.random().scale(SMOKE_SPREAD / 10));
            entityDeposit.addSpawn(new InvisibleEntity.Factory(position, move, SMOKE_LINGER_TIME));
        }
    }

    private void launchSeekers() {
        float min = -1;
        MovingEntity tgt = null;
        PosVector pos = getPosition();

        for (MovingEntity entity : entityMapping) {
            if (entity == this || entity instanceof AbstractProjectile || entity instanceof PowerupEntity) continue;

            Vector3f relPos = entity.getPosition().sub(pos).normalize();
            float dot = getForward().dot(relPos);

            if (dot > min) {
                min = dot;
                tgt = entity;
            }
        }

        for (int i = 0; i < ServerSettings.NOF_SEEKERS_LAUNCHED; i++) {
            DirVector randDirection = DirVector.random().scale(PowerupType.SEEKER_LAUNCH_SPEED);
            randDirection.add(velocity);
            State interpolator = new State(position, extraPosition, Toolbox.xTo(randDirection), rotation, randDirection, getForward());

            Seeker.Factory newSeeker = new Seeker.Factory(interpolator, 0, this, tgt);
            entityDeposit.addSpawn(newSeeker);
        }
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
        gl.setMaterial(surfaceMaterial);
    }

    public void set(State spawn) {
        set(spawn.position(0), spawn.velocity(), spawn.rotation(0), 0);
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
}
