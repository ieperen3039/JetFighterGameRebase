package nl.NG.Jetfightergame.AbstractEntities;

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
public abstract class AbstractProjectile extends GameEntity implements TemporalEntity, Serializable {

    protected static final float IMPACT_POWER = 5f;
    private static final int SPARK_DENSITY = 10;
    protected Material surfaceMaterial;
    private DirVector forward;
    private final float airResistCoeff;

    protected float timeToLive;

    public AbstractProjectile(
            int id, PosVector initialPosition, Quaternionf initialRotation, DirVector initialVelocity, float scale,
            float mass, Material surfaceMaterial, float airResistCoeff, float timeToLive, GameTimer gameTimer,
            SpawnReceiver particleDeposit
    ) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, scale, gameTimer, particleDeposit);
        this.airResistCoeff = airResistCoeff;
        this.timeToLive = timeToLive;
        this.surfaceMaterial = surfaceMaterial;

        forward = new DirVector();
        relativeStateDirection(DirVector.xVector()).normalize(forward);
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        timeToLive -= deltaTime;

        netForce.add(forward.reducedTo(getThrust(forward), new DirVector()), netForce);

        DirVector airResistance = new DirVector();
        float speed = velocity.length();
        velocity.reducedTo(speed * speed * airResistCoeff * -1, airResistance);
        velocity.add(airResistance.scale(deltaTime, airResistance));
        relativeStateDirection(DirVector.xVector()).normalize(forward);

        adjustOrientation(extraPosition, extraRotation, extraVelocity, forward, deltaTime);

        // collect extrapolated variables
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public ParticleCloud explode() {
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

    /**
     * update state of this projectile according to input. This method is called after the physics are applied
     * @param extraPosition the place to store the new position
     * @param extraRotation the place to store the new rotation
     * @param extraVelocity the place to store the new velocity
     * @param forward       vector pointing along the x-axis of the projectile in world-space
     * @param deltaTime     time difference
     */
    protected abstract void adjustOrientation(
            PosVector extraPosition, Quaternionf extraRotation, DirVector extraVelocity, DirVector forward, float deltaTime);

    protected abstract float getThrust(DirVector forward);

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }
}
