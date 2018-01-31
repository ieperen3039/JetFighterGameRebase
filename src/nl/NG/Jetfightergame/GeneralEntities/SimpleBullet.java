package nl.NG.Jetfightergame.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Primitives.Particles.Particles;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen
 * created on 28-1-2018.
 */
public class SimpleBullet extends AbstractProjectile {

    private static final float MASS = 0.1f;
    private static final float AIR_RESIST_COEFF = 0.01f;

    public SimpleBullet(PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation, GameTimer gameTimer) {
        super(Material.SILVER, MASS, 1, initialPosition, initialVelocity, initialRotation, gameTimer, AIR_RESIST_COEFF, 1000 / initialVelocity.length());
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
        ShadowMatrix ms = new ShadowMatrix();
        ms.translate(getPosition());

        return GeneralShapes.CUBE.getPlanes()
//                .parallel()
                .map(p -> Particles.generateFireParticles(10f, ms, p, 0.5f))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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
