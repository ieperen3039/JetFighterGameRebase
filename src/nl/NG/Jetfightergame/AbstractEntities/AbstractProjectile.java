package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static nl.NG.Jetfightergame.Settings.ClientSettings.*;
import static nl.NG.Jetfightergame.Tools.Toolbox.instantPreserveFraction;

/**
 * @author Geert van Ieperen created on 24-1-2018.
 */
public abstract class AbstractProjectile extends MovingEntity implements TemporalEntity, Serializable {

    protected static final float IMPACT_POWER = 5f;
    private static final int SPARK_DENSITY = 10;
    private Material surfaceMaterial;
    protected DirVector forward;
    private final float airResistCoeff;

    protected float timeToLive;
    private final float turnAcc;
    private final float rollAcc;
    protected Controller controller;
    private float thrustPower;

    private final MovingEntity sourceJet;
    protected MovingEntity target = null;
    private float rotationPreserveFactor;

    /**
     * a projectile has no planes (it cannot be hit) and only one hitpoint on (0,0,0). An instance is initially
     * controlled by a {@link JustForward} instance, but can be set to any controller using {@link
     * #setController(Controller)}
     * @param id                unique identifier for this entity
     * @param initialPosition   position of spawning (of the origin) in world coordinates
     * @param initialRotation   the initial rotation of spawning
     * @param initialVelocity   the initial velocity, that is the vector of movement per second in world-space
     * @param mass              the mass of the object in kilograms.
     * @param surfaceMaterial   the default material properties of the whole object.
     * @param airResistCoeff    Air resistance coefficient Cw as in Fwl = 0.5 * A * Cw.
     * @param timeToLive        time before this entity returns true when calling {@link #isOverdue()}
     * @param turnAcc           acceleration over yaw or pitch axis when applying full power in rad/ss
     * @param rollAcc           roll acceleration when rolling with full power in rad/ss
     * @param thrustPower       the power when full throttle is requested in Newton
     * @param rotationReduction the fraction that the rotation of this object is slowed down every second
     * @param particleDeposit   particles are passed here
     * @param gameTimer         the timer that determines the "current rendering time" for {@link
     *                          MovingEntity#interpolatedPosition()}
     * @param sourceEntity      the entity that launched this projectile
     */
    public AbstractProjectile(
            int id, PosVector initialPosition, Quaternionf initialRotation, DirVector initialVelocity,
            float mass, Material surfaceMaterial, float airResistCoeff, float timeToLive, float turnAcc, float rollAcc, float thrustPower,
            float rotationReduction, SpawnReceiver particleDeposit, GameTimer gameTimer, MovingEntity sourceEntity
    ) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, 1, gameTimer, particleDeposit);
        this.airResistCoeff = airResistCoeff;
        this.timeToLive = timeToLive;
        this.surfaceMaterial = surfaceMaterial;
        this.turnAcc = turnAcc;
        this.rollAcc = rollAcc;
        this.thrustPower = thrustPower;
        this.rotationPreserveFactor = 1 - rotationReduction;
        this.controller = new JustForward();
        this.sourceJet = sourceEntity;

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    public static List<EntityFactory> createCloud(MovingEntity source, int nOfProjectiles, float launchSpeed, Function<EntityState, EntityFactory> factory) {
        List<EntityFactory> projectiles = new ArrayList<>(nOfProjectiles);

        for (int i = 0; i < nOfProjectiles; i++) {
            DirVector randDirection = DirVector.random().scale(launchSpeed);
            randDirection.add(source.getVelocity());

            EntityState interpolator = new EntityState(source.getPosition(), randDirection, randDirection);
            projectiles.add(factory.apply(interpolator));
        }

        return projectiles;
    }

    public void setController(Controller con) {
        this.controller = con;
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        relativeStateDirection(DirVector.xVector()).normalize(forward);
        DirVector temp = new DirVector();
        controller.update();

        // thrust forces
        float throttle = controller.throttle();
        float thrust = (throttle > 0) ? throttle * thrustPower : 0;
        netForce.add(forward.reducedTo(thrust, temp), netForce);

        // transform velocity to local, reduce drifting, then transform back to global space
        float red = 0.1f * deltaTime;
        reduceDriftLinear(extraVelocity, red, red);

        float rotationPreserveFraction = instantPreserveFraction(rotationPreserveFactor, deltaTime);
        yawSpeed *= rotationPreserveFraction;
        pitchSpeed *= rotationPreserveFraction;
        rollSpeed *= rotationPreserveFraction;

        // rotational forces
        float instYawAcc = turnAcc * deltaTime;
        float instPitchAcc = turnAcc * deltaTime;
        float instRollAcc = rollAcc * deltaTime;
        yawSpeed += controller.yaw() * instYawAcc;
        pitchSpeed += controller.pitch() * instPitchAcc;
        rollSpeed += controller.roll() * instRollAcc;

        // air-resistance
        DirVector airResistance = new DirVector();
        float speedSq = velocity.lengthSquared();
        velocity.reducedTo(speedSq * airResistCoeff * -1, airResistance);
        netForce.add(airResistance);

        // F = m * a ; a = dv/dt
        // a = F/m ; dv = a * dt = F * (dt/m)
        extraVelocity.add(netForce.scale(deltaTime / mass, temp));

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, temp), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    private void reduceDriftLinear(DirVector ev, float yReduction, float zReduction) {
        Quaternionf turnBack = rotation.invert(new Quaternionf());
        ev.rotate(turnBack);
        float ny = ev.y - yReduction;
        float nz = ev.z - zReduction;
        ev.set(ev.x, ny > 0 ? ny : 0, nz > 0 ? nz : 0);
        ev.rotate(rotation);
    }

    @Override
    public ParticleCloud explode() {
        timeToLive = 0;
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(
                interpolatedPosition(), DirVector.zeroVector(),
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2,
                IMPACT_POWER, SPARK_DENSITY, Particles.FIRE_LINGER_TIME, FIRE_PARTICLE_SIZE
        );
    }

    @Override
    public boolean isOverdue() {
        return timeToLive <= 0;
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeToLive -= deltaTime;

    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return extraPosition;
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        if (other == sourceJet) return null;

        if (super.checkCollisionWith(other, deltaTime) != null) {
            timeToLive = 0;
            collideWithOther(other);
        }

        return null;
    }

    protected abstract void collideWithOther(Touchable other);

    /** a controller that returns throttle = 1, and 0 for all else */
    public static class JustForward extends Controller.EmptyController {
        @Override
        public float throttle() {
            return 1;
        }
    }

    public static abstract class RocketFactory extends EntityFactory {
        protected int sourceID = -1;
        protected int targetID = -1;

        protected RocketFactory() {
            super();
        }

        public RocketFactory(EntityClass type, EntityState state, float nlerpFrac, MovingEntity source, MovingEntity target) {
            super(type, state, nlerpFrac);
            sourceID = source.idNumber();
            targetID = target != null ? target.idNumber() : -1;
        }

        public RocketFactory(EntityClass type, AbstractProjectile projectile) {
            super(type, projectile);
            sourceID = projectile.sourceJet.idNumber();
            targetID = projectile.target.idNumber();
        }

        @Override
        public void writeInternal(DataOutput out) throws IOException {
            if (sourceID == -1) throw new NullPointerException("Both source and target of the seeker must be set");
            super.writeInternal(out);
            out.writeInt(sourceID);
            out.writeInt(targetID);
        }

        @Override
        public void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            sourceID = in.readInt();
            targetID = in.readInt();
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            MovingEntity src = entities.getEntity(sourceID);
            MovingEntity tgt = entities.getEntity(targetID);
            return construct(game, src, tgt);
        }

        public abstract MovingEntity construct(SpawnReceiver game, MovingEntity src, MovingEntity tgt);
    }
}
