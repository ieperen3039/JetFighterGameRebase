package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 2-3-2018.
 */
public class StaticEntity implements Touchable {

    private final Shape shape;
    protected final Material material;
    protected Color4f color;
    private final Vector offSet;
    private final float scaling;
    private final Quaternionf rotation;

    private final PosVector relativeMiddle;
    private final float range;

    /** @see #StaticEntity(Shape, Material, Color4f, Vector, float, Quaternionf) */
    public StaticEntity(Shape shape, Material material, Color4f color) {
        this(shape, material, color, null, 1, null);
    }

    /** @see #StaticEntity(Shape, Material, Color4f, Vector, float, Quaternionf) */
    public StaticEntity(Shape shape, Material material, Color4f color, Vector position) {
        this(shape, material, color, position, 1, null);
    }

    /** @see #StaticEntity(Shape, Material, Color4f, Vector, float, Quaternionf) */
    public StaticEntity(Shape shape, Material material, Color4f color, PosVector position, float scaling) {
        this(shape, material, color, position, scaling, null);
    }

    /**
     * creates an entity with the given shape. The entity is not movable, but implements collision detection.
     * @param shape   the shape to represent
     * @param material the surface material
     * @param color    the color modifier
     * @param offSet   the position of the origin of the shape, or null for (0, 0, 0)
     * @param scaling  the scaling factor
     * @param rotation the rotation of the object, or null for no rotation
     */
    public StaticEntity(Shape shape, Material material, Color4f color, Vector offSet, float scaling, Quaternionf rotation) {
        this.shape = shape;
        this.material = material;
        this.color = color;
        this.scaling = scaling;
        this.offSet = ((offSet == null) || !offSet.isScalable()) ? null : offSet;
        this.rotation = ((rotation == null) || (rotation.lengthSquared() == 0)) ? null : rotation;

        Pair<PosVector, Float> hitbox = Shape.getMinimalCircle(shape.getPoints());

        this.relativeMiddle = hitbox.left;
        this.range = hitbox.right * scaling;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        action.accept(shape);
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        ms.pushMatrix();
        {
            if (offSet != null) ms.translate(offSet);
            ms.scale(scaling);
            if (rotation != null) ms.rotate(rotation);
            action.run();
        }
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(material, color);
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public PosVector getExpectedMiddle() {
        PosVector result = new PosVector(relativeMiddle);
        if (offSet != null) result.add(offSet);
        return result;
    }

    @Override
    public String toString() {
        return shape.toString();
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
