package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Primitives.Particles.Particles;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.Color4f;
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

    public SimpleBullet(PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation, GameTimer gameTimer) {
        super(Material.SILVER, 100, 1, initialPosition, initialVelocity, initialRotation, gameTimer, 0.1f, 1000 / initialVelocity.length());
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
                .map(p -> Particles.splitIntoParticles(p, 2, DirVector.randomOrb(), 0, 2, 5f, Color4f.YELLOW, ms))
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
