package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

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
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_1;
import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_2;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleRocket extends AbstractProjectile {
    public static final String TYPE = "Simple rocket";

    private static final float MASS = 5f;
    private static final float AIR_RESIST_COEFF = 0.01f;
    private static final float IMPACT_POWER = 5f;
    private static final float EXPLOSION_POWER = 20f;
    private static final int DENSITY = 1000;
    private static final int THRUST = 100;

    /**
     * enables the use of 'Simple rocket'
     */
    public static void init() {
        addConstructor(TYPE, (id, position, rotation, velocity, game) ->
                new SimpleRocket(id, position, velocity, rotation, game.getTimer(), game));
    }

    private SimpleRocket(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                         GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, 1f, MASS, Material.SILVER,
                AIR_RESIST_COEFF, 10,
                0, new JustForward(), THRUST, entityDeposit, gameTimer
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
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, EXPLOSION_POWER, DENSITY
        );
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeToLive -= deltaTime;
        // sparkles
    }

    @Override
    protected PairList<PosVector, PosVector> calculateHitpointMovement() {
        PairList<PosVector, PosVector> pairs = new PairList<>(1);
        pairs.add(position, extraPosition);
        return pairs;
    }

    @Override
    public float getRange() {
        return 0;
    }

}
