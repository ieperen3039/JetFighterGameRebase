package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
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
public class SimpleRocket extends AbstractProjectile {

    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0.001f;
    private static final float IMPACT_POWER = 20;
    private static final int DENSITY = 1000;

    public SimpleRocket(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                        GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, 1f, MASS, Material.SILVER,
                AIR_RESIST_COEFF, 10,
                gameTimer, entityDeposit
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            ms.translate(-0.5f, 0, 0);
            action.accept(GeneralShapes.ARROW);
        }
        ms.popMatrix();
    }

    /**
     * @param other an object that may hit this object
     * @return null
     */
    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
//        if (other instanceof Projectile); // reward 'crimera war' achievement

        if (super.checkCollisionWith(other, deltaTime) != null) {
            other.impact(IMPACT_POWER);
            this.timeToLive = 0;
        }
        // there is no physical effect of projectile impact
        return null;
    }

    @Override
    public ParticleCloud explode() {
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(
                interpolatedPosition(), DirVector.zeroVector(),
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, IMPACT_POWER, DENSITY
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
    protected List<TrackedVector<PosVector>> calculateHitpointMovement() {
        return Collections.singletonList(new TrackedVector<>(position, extraPosition));
    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    protected void adjustOrientation(PosVector extraPosition, Quaternionf extraRotation, DirVector extraVelocity, DirVector forward, float deltaTime) {

    }

    @Override
    protected float getThrust(DirVector forward) {
        return 100;
    }
}
