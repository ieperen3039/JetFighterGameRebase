package nl.NG.Jetfightergame.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Vectors.Color4f;

import java.util.function.Consumer;

/**
 * An immovable entity-representation of an inverse cube
 * @author Geert van Ieperen
 *         created on 8-11-2017.
 */
public class ContainerCube implements Touchable {
    public final int labSize;
    private final Shape world;
    private final Material material = Material.PLASTIC;

    public ContainerCube(int cubeSize) {
        super();
        this.labSize = cubeSize;
        world = GeneralShapes.INVERSE_CUBE;
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(material, Color4f.ORANGE);
    }

    @Override
    public void acceptCollision(Collision cause) {
        // ignore collisions
    }

    @Override
    public RigidBody getFinalCollision(float deltaTime) {
        return new RigidBody(this);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.scale(labSize);
            // just accept the world bruh
            action.accept(world);
        }
        ms.popMatrix();
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        action.run();
    }

    @Override
    public String toString() {
        return "Testlab {" +
                "size: " + labSize + "}";
    }
}
