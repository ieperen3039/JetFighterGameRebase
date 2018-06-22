package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractProjectile {

    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0f;

    public SimpleBullet(int id, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                        GameTimer gameTimer, SpawnReceiver entityDeposit
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, 1f, MASS, Material.SILVER,
                AIR_RESIST_COEFF, 10, gameTimer, entityDeposit
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
        return 0;
    }
}
