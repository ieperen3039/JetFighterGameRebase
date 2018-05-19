package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.Projectile;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Sounds;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromFile;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_1;
import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_2;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractJet implements Projectile {

    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0.01f;
    private static final float DIRECTION_STRAIGHTEN = 0.01f;
    private static final float IMPACT_POWER = 1f;
    private float timeToLive = 10f;

    public SimpleBullet(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                        GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        super(
                id, Controller.EMPTY, initialPosition, initialRotation, initialVelocity, 1f, Material.SILVER, MASS,
                AIR_RESIST_COEFF, DIRECTION_STRAIGHTEN,
                gameTimer, entityDeposit, 0
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            ms.translate(-0.5f, 0, 0);
            action.accept(ShapeFromFile.ARROW);
        }
        ms.popMatrix();
    }

    @Override
    public boolean checkCollisionWith(Touchable other, float deltaTime) {
//        if (other instanceof Projectile); // reward 'crimera war' achievement

        if (super.checkCollisionWith(other, deltaTime)){
            other.impact(IMPACT_POWER);
            this.timeToLive = 0;
        }
        // there is no physical effect of projectile impact
        return false;
    }

    @Override
    public ParticleCloud explode() {
        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(position, velocity, EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, IMPACT_POWER);
    }

    @Override
    public boolean isDead() {
        return timeToLive <= 0;
    }

    @Override
    public PosVector getPilotEyePosition() {
        return interpolatedPosition();
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeToLive -= deltaTime;
    }

    @Override
    protected List<TrackedVector<PosVector>> calculateHitpointMovement() {
        return Collections.singletonList(new TrackedVector<>(position, extraPosition));
    }

    @Override
    public float getRange() {
        return 0;
    }
}
