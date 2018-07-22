package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.Serializable;

import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_2;

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
    private float turnAcc;
    private Controller controller;
    private float thrustPower;

    public AbstractProjectile(
            int id, PosVector initialPosition, Quaternionf initialRotation, DirVector initialVelocity, float scale,
            float mass, Material surfaceMaterial, float airResistCoeff, float timeToLive, float turnAcc, float thrustPower,
            SpawnReceiver particleDeposit, GameTimer gameTimer
    ) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, scale, gameTimer, particleDeposit);
        this.airResistCoeff = airResistCoeff;
        this.timeToLive = timeToLive;
        this.surfaceMaterial = surfaceMaterial;
        this.turnAcc = turnAcc;
        this.thrustPower = thrustPower;
        this.controller = new JustForward();

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    public void setController(Controller con) {
        this.controller = con;
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        DirVector temp = new DirVector();
        controller.update();

        // thrust forces
        float throttle = controller.throttle();
        float thrust = (throttle > 0) ? throttle * thrustPower : 0;
        netForce.add(forward.reducedTo(thrust, temp), netForce);

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

    @Override
    public ParticleCloud explode() {
        timeToLive = 0;
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(interpolatedPosition(), DirVector.zeroVector(), Color4f.WHITE, EXPLOSION_COLOR_2, IMPACT_POWER, SPARK_DENSITY);
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
        Collision collision = super.checkCollisionWith(other, deltaTime);
        if (collision != null) {
            collideWithOther(other, collision);
        }
        return null;
    }

    /** progress a collision with the given entity. */
    protected abstract void collideWithOther(Touchable other, Collision collision);

    /** a controller that returns throttle = 1, and 0 for all else */
    public static class JustForward extends Controller.EmptyController {
        @Override
        public float throttle() {
            return 1;
        }
    }
}
