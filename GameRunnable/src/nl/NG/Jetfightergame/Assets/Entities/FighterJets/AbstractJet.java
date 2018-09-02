package nl.NG.Jetfightergame.Assets.Entities.FighterJets;

import nl.NG.Jetfightergame.Assets.Entities.AbstractShield;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
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
import nl.NG.Jetfightergame.Rendering.Particles.DataIO;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Sound.MovingAudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothFloat;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType.NONE;
import static nl.NG.Jetfightergame.Settings.ClientSettings.*;
import static nl.NG.Jetfightergame.Settings.ServerSettings.GENERAL_SPEED_FACTOR;
import static nl.NG.Jetfightergame.Settings.ServerSettings.INTERPOLATION_QUEUE_SIZE;

/**
 * @author Geert van Ieperen created on 30-10-2017.
 */
public abstract class AbstractJet extends MovingEntity {
    private static final float BOOSTER_PITCH = 4f;
    private static final float PITCH_RAISE_FACTOR = 0.8f;
    public static final float BOOSTER_GAIN = 2f;
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
    private MovingAudioSource boosterSound;
    private ExponentialSmoothFloat boostPitch;

    /** left = factor , right = duration */
    private Collection<Pair<Float, Float>> speedModifiers;

    private DirVector forward;
    private VectorInterpolator forwardInterpolator;
    private VectorInterpolator velocityInterpolator;

    private PowerupType currentPowerup = PowerupType.NONE;
    protected Color4f color;

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
     * @param gameTimer               the game timer
     * @param yReduction              reduces drifting/stalling in horizontal direction by this fraction
     * @param zReduction              reduces drifting/stalling in vertical direction by this fraction
     * @param entityDeposit           the class that allows new entityMapping and particles to be added to the
     *                                environment
     * @param entityMapping           a mapping that allows binding id's to entityMapping
     * @param color
     */
    public AbstractJet(
            int id, PosVector initialPosition, Quaternionf initialRotation,
            Material material, float mass, float airResistanceCoeff,
            float throttlePower, float brakePower, float yawAcc, float pitchAcc, float rollAcc,
            float rotationReductionFactor, GameTimer gameTimer, float yReduction, float zReduction,
            SpawnReceiver entityDeposit, EntityMapping entityMapping, Color4f color
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
        this.color = color;

        float time = gameTimer.time();
        forward = DirVector.xVector();
        forward.rotate(rotation);
        speedModifiers = new HashSet<>();
        nuzzle = new ArrayList<>();

        if (!entityDeposit.isHeadless()) {
            forwardInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, new DirVector(forward), time);
            velocityInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, DirVector.zeroVector(), time);
            boosterSound = getBoosterSound();
            boostPitch = new ExponentialSmoothFloat(0.01f, PITCH_RAISE_FACTOR);
            entityDeposit.add(boosterSound);
        }
    }

    @Override
    public void applyPhysics(DirVector netForce) {
        controller.update();

        float time = gameTimer.time();
        float deltaTime = gameTimer.getGameTime().difference();
        speedModifiers.removeIf(p -> p.right < time);

        gyroPhysics(deltaTime, netForce, velocity);

        relativeDirection(DirVector.xVector()).normalize(forward);
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
        rotation.rotate(rollSpeed * deltaTime, -pitchSpeed * deltaTime, -yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public void addStatePoint(float currentTime, PosVector newPosition, Quaternionf newRotation) {
        super.addStatePoint(currentTime, newPosition, newRotation);
        forward = DirVector.xVector();
        forward.rotate(rotation);
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
        if (entityDeposit.isHeadless()) {
            return new DirVector(forward);
        } else {
            forwardInterpolator.updateTime(renderTime());
            return forwardInterpolator.getInterpolated(renderTime()).toDirVector();
        }
    }

    @Override
    public DirVector getVelocity() {
        if (entityDeposit.isHeadless()) {
            return super.getVelocity();
        } else {
            velocityInterpolator.updateTime(renderTime());
            return velocityInterpolator.getInterpolated(renderTime()).toDirVector();
        }
    }

    /**
     * @param nOfBoosters total number of boosters
     * @param left        first relative position of booster
     * @param right       second relative position of booster
     */
    protected void addBooster(int nOfBoosters, PosVector left, PosVector right) {
        float pps = (BASE_THRUST_PPS * PARTICLE_MODIFIER) / nOfBoosters;
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
        gl.setMaterial(Material.ROUGH, color);

        DirVector trail = getForward();
        trail.mul(-JET_THRUST_SPEED).add(getVelocity());

        float thrust = 0.5f * (controller.throttle() + 1.0f);
        // apply trust modifiers
        for (Pair<Float, Float> modifier : speedModifiers) {
            thrust *= modifier.left;
        }
        float pps = Math.max((BASE_THRUST_PPS * PARTICLE_MODIFIER * thrust * thrust) / nuzzle.size(), 3);

        PosVector currPos = getPosition();
        Quaternionf currRot = getRotation();

        MatrixStack sm = new ShadowMatrix();
        toLocalSpace(sm, () -> nuzzle.forEach(boosterLine ->
                entityDeposit.add(boosterLine.update(sm, trail, 0.1f, pps))), currPos, currRot
        );

        float tgt = Math.max(0.1f, thrust * thrust * BOOSTER_PITCH);
        boostPitch.updateFluent(tgt, gameTimer.getRenderTime().difference());

        if (boosterSound.isOverdue()) {
            boostPitch.update(0.01f);
            Logger.DEBUG.print(boosterSound);
            boosterSound = getBoosterSound();
            entityDeposit.add(boosterSound);
            Logger.DEBUG.print(boosterSound);
        } else {
            boosterSound.setPitch(boostPitch.current());
        }
    }

    protected abstract MovingAudioSource getBoosterSound();

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
        PowerupType next = currentPowerup.with(type);
        if (next == currentPowerup || next == NONE) return false;
        currentPowerup = next;
        return true;
    }

    public void setPowerup(PowerupType color) {
        if (!entityDeposit.isHeadless()) {
            // powerup sound
            if (color != NONE) {
                Toolbox.checkALError();
                if (currentPowerup == NONE) {
                    entityDeposit.add(new AudioSource(Sounds.powerupOne.get(), 0.5f, false));

                } else {
                    entityDeposit.add(new AudioSource(Sounds.powerupTwo.get(), 0.5f, false));
                }

            } else if (currentPowerup != NONE) {
                AudioSource sound = currentPowerup.launchSound(this);
                if (sound != null) entityDeposit.add(sound);
            }
        }
        currentPowerup = color;
    }

    private void usePowerup() {
        currentPowerup.activate(this, entityDeposit);

        currentPowerup = PowerupType.NONE;
        entityDeposit.playerPowerupState(this, PowerupType.NONE);
    }

    public void setBoosterColor(Color4f color1, Color4f color2, float duration) {
        nuzzle.forEach(n -> n.setColor(color1, color2, duration));
    }

    public MovingEntity getTarget(EntityState s) {
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

        return String.format("(%3d) [ %s ] %s", idNumber(), toString(), boost);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public void setJetColor(Color4f color) {
        this.color = color;
    }

    protected static abstract class JetFactory extends EntityFactory {
        protected Color4f color;

        public JetFactory(EntityClass type, PosVector pos, Quaternionf rot, DirVector vel, Color4f color) {
            super(type, pos, rot, vel);
            this.color = color;
        }

        public JetFactory(EntityClass type, AbstractJet jet) {
            super(type, jet);
            color = jet.color;
        }

        public JetFactory() {
            color = Color4f.WHITE;
        }

        public JetFactory(EntityClass type, EntityState spawnPosition, int timeFraction, Color4f color) {
            this(type,
                    spawnPosition.position(timeFraction),
                    spawnPosition.rotation(timeFraction),
                    spawnPosition.velocity(timeFraction),
                    color
            );
        }

        @Override
        protected void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            DataIO.writeColor(out, color);
        }

        @Override
        protected void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            color = DataIO.readColor(in);
        }
    }
}
