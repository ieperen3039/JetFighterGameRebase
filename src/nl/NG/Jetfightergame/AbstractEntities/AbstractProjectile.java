package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
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

import static nl.NG.Jetfightergame.Settings.ClientSettings.*;
import static nl.NG.Jetfightergame.Tools.Toolbox.instantPreserveFraction;

/**
 * @author Geert van Ieperen created on 24-1-2018.
 */
public abstract class AbstractProjectile extends MovingEntity implements TemporalEntity, Serializable {

    protected static final float IMPACT_POWER = 5f;
    private static final int SPARK_DENSITY = 10;
    private Material surfaceMaterial;
    private DirVector forward;
    private final float airResistCoeff;

    protected float timeToLive;
    private float turnAcc;
    private Controller controller;
    private float thrustPower;

    private final MovingEntity sourceJet;
    private int targetID = -1;
    private float rotationPreserveFactor;

    public AbstractProjectile(
            int id, PosVector initialPosition, Quaternionf initialRotation, DirVector initialVelocity, float scale,
            float mass, Material surfaceMaterial, float airResistCoeff, float timeToLive, float turnAcc, float thrustPower,
            float rotationReduction, SpawnReceiver particleDeposit, GameTimer gameTimer, MovingEntity sourceJet
    ) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, scale, gameTimer, particleDeposit);
        this.airResistCoeff = airResistCoeff;
        this.timeToLive = timeToLive;
        this.surfaceMaterial = surfaceMaterial;
        this.turnAcc = turnAcc;
        this.thrustPower = thrustPower;
        this.rotationPreserveFactor = 1 - rotationReduction;
        this.controller = new JustForward();
        this.sourceJet = sourceJet;

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
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

        float rotationPreserveFraction = instantPreserveFraction(rotationPreserveFactor, deltaTime);
        yawSpeed *= rotationPreserveFraction;
        pitchSpeed *= rotationPreserveFraction;
        rollSpeed *= rotationPreserveFraction;

        // rotational forces
        float instYawAcc = turnAcc * deltaTime;
        float instPitchAcc = turnAcc * deltaTime;
        float instRollAcc = turnAcc * deltaTime;
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

    public void setTarget(MovingEntity target) {
        targetID = target.idNumber();
        setController(new RocketAI(this, target, 200f, false));
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

        public RocketFactory(EntityClass type, State state, float fraction, MovingEntity source, MovingEntity target) {
            super(type, state, fraction);
            sourceID = source.idNumber();
            targetID = target != null ? target.idNumber() : -1;
        }

        public RocketFactory(EntityClass type, AbstractProjectile projectile) {
            super(type, projectile);
            sourceID = projectile.sourceJet.idNumber();
            targetID = projectile.targetID;
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
