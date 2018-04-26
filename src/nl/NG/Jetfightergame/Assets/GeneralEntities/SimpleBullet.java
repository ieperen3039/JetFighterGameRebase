package nl.NG.Jetfightergame.Assets.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GameState.EntityManager;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Primitives.Particles.FireParticle;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromMesh;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractProjectile {

    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0.01f;

    public SimpleBullet(PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation,
                        GameTimer gameTimer, EntityManager particleDeposit
    ) {
        super(
                Material.SILVER, MASS, 1, initialPosition, initialVelocity, initialRotation, gameTimer, AIR_RESIST_COEFF,
                1000 / initialVelocity.length(), particleDeposit
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            action.accept(ShapeFromMesh.ARROW);
        }
        ms.popMatrix();
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public Collection<Particle> explode() {
        List<Particle> result = new ArrayList<>(5);

        for (int i = 0; i < ClientSettings.SPARK_PARTICLE_DENSITY; i++) {
            result.add(FireParticle.randomParticle(getPosition(), 10, 1));
        }
        timeToLive = 0;

        return result;
    }

    @Override
    public void hit(Touchable other, PosVector hitPosition) {
        if (other instanceof GameEntity){
            ((GameEntity) other).impact(hitPosition, 20);
        }
    }

    @Override
    protected void adjustOrientation(PosVector extraPosition, Quaternionf extraRotation, DirVector extraVelocity, DirVector forward, float deltaTime) {

    }

    @Override
    protected float getThrust(DirVector forward) {
        return 0;
    }
}
