package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    private final Vector3f scaling;
    private final Quaternionf rotation;

    private final PosVector middle;
    private final float range;

    /** @see #StaticObject(Shape, Material, Color4f, Vector, Vector3f, Quaternionf) */
    public StaticObject(Shape source, PosVector position, Material material, Color4f color, float scaling) {
        this(source, material, color, position, new Vector3f(scaling, scaling, scaling), null);
    }

    /** @see #StaticObject(Shape, Material, Color4f, Vector, Vector3f, Quaternionf) */
    public StaticObject(Shape source, Material material, Color4f color) {
        this(source, material, color, null, null, null);
    }

    /**
     * creates an entity with the given shape. The entity is not movable, but implements collision detection.
     * @param source   the shape to represent
     * @param material the surface material
     * @param color    the color modifier
     * @param offSet   the position of the origin of the shape, or null for (0, 0, 0)
     * @param scaling  the scaling factor, in xyz directions, or null for (0, 0, 0)
     * @param rotation the rotation of the object, or null for no rotation
     */
    public StaticObject(Shape source, Material material, Color4f color, Vector offSet, Vector3f scaling, Quaternionf rotation) {
        this.source = source;
        this.material = material;
        this.color = color;
        this.offSet = ((offSet != null) && offSet.isScalable()) ? offSet : null;
        this.scaling = ((scaling != null) && (scaling.lengthSquared() != 0)) ? scaling : null;
        this.rotation = ((rotation != null) && (rotation.lengthSquared() != 0)) ? rotation : null;

        Pair<PosVector, Float> hitbox = getMinimalCircle(source.getPoints());

        this.middle = hitbox.left;
        this.range = hitbox.right;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            if (rotation != null) ms.rotate(rotation);
            if (offSet != null) ms.translate(offSet);
            if (scaling != null) ms.scale(scaling);
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
    public float getRange() {
        return range;
    }

    @Override
    public PosVector getExpectedPosition() {
        return middle;
    }

    /**
     * Calculates the smallest orb around the given points
     * @param points a number of points, at least two
     * @return Left, the middle of the found orb.
     * Right, the radius of the given orb
     */
    public static Pair<PosVector, Float> getMinimalCircle(Iterable<PosVector> points) {
        // determine furthest two point
        float duoMax = 0;
        DirVector temp = new DirVector();
        PosVector aMax = new PosVector();
        PosVector bMax = new PosVector();
        for (PosVector a : points) {
            for (PosVector b : points) {
                float dist = a.to(b, temp).lengthSquared();
                if (dist > duoMax) {
                    duoMax = dist;
                    aMax.set(a);
                    bMax.set(b);
                }
            }
        }

        // determine point furthest from the middle
        PosVector mid = aMax.middleTo(bMax);
        PosVector outer = new PosVector();
        float tripleMax = 0;
        for (PosVector vector : points) {
            float dist = mid.to(vector, temp).lengthSquared();
            if (dist > tripleMax) {
                outer.set(vector);
                tripleMax = dist;
            }
        }

        // if this point is none of the two previous points, determine the circumscribed circle
        // https://en.wikipedia.org/wiki/Circumscribed_circle
        if ((tripleMax > (duoMax / 4)) && !(outer.equals(aMax) || outer.equals(bMax))) {
            PosVector temp2 = new PosVector();
            PosVector temp3 = new PosVector();

            PosVector a = aMax.sub(outer, new PosVector());
            PosVector b = bMax.sub(outer, new PosVector());

            PosVector dif = b.scale(a.lengthSquared(), temp2)
                    .sub(a.scale(b.lengthSquared(), temp3), temp2);
            float scalar = 2 * a.cross(b, temp3).lengthSquared();

            mid.set(
                    dif.cross(a.cross(b, temp)).div(scalar).add(outer)
            );
        }

        return new Pair<>(mid, mid.to(aMax, temp).length());
    }
}
