package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 2-3-2018.
 */
public class StaticObject implements Touchable {

    private final Shape source;
    private final Material material;
    private final Color4f color;
    private final Vector offSet;
    private final float scaling;

    public StaticObject(Shape source, Material material, Color4f color, float scaling) {
        this(source, material, color, DirVector.zeroVector(), scaling);
    }

    public StaticObject(Shape source, Material material, Color4f color) {
        this(source, material, color, 1);
    }

    public StaticObject(Shape source, Material material, Color4f color, Vector offSet, float scaling) {
        this.source = source;
        this.material = material;
        this.color = color;
        this.offSet = offSet;
        this.scaling = scaling;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            if (scaling != 0) ms.scale(scaling);
            ms.translate(offSet);
            action.accept(source);
        }
        ms.popMatrix();
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        action.run();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(material, color);
    }

    @Override
    public void acceptCollision(Collision cause) {

    }

    @Override
    public RigidBody getFinalCollision(float deltaTime) {
        return new RigidBody(this);
    }
}
