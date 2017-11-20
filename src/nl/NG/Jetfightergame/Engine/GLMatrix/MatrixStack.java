package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix4f;

/**
 * @author Geert van Ieperen
 *         created on 19-11-2017.
 */
public interface MatrixStack {
    /** TODO overwrite in GLShader impl.
     * rotates the axis frame such that the z-axis points from source to target vector,
     * and translates the system to source
     * if (target == source) the axis will not turn
     * @param source the vector where the axis will have its orgin upon returning
     * @param target the vector in which direction the z-axis will point upon returning
     */
    default void pointFromTo(PosVector source, PosVector target) {
        if (target.equals(source)) return;
        DirVector parallelVector = source.to(target)
                .normalized();

        DirVector M = DirVector.Z.cross(parallelVector);
        double angle = Math.acos(DirVector.Z.dot(parallelVector));// in Radians

        translate(source);
        rotate(M, angle);
    }

    /**
     * rotates the coordinate system along the axis defined by (x, y, z)
     * @param angle in radians
     */
    void rotate(double angle, float x, float y, float z);

    /**
     * moves the system x to the x-axis, y to the y-asis and z toward the z-axis
     */
    void translate(float x, float y, float z);

    /**
     * scale in the direction of the appropriate axes
     */
    void scale(float x, float y, float z);

    /**
     * calculate the position of a vector in reference to the space in which this is initialized
     * @param p a local vector
     * @return that vector in world-space
     */
    PosVector getPosition(PosVector p);

    /**
     * calculate a direction vector in reference to the space in which this is initialized
     * @param v a local direction
     * @return this direction in world-space
     */
    DirVector getDirection(DirVector v);

    void pushMatrix();

    void popMatrix();

    default void rotate(DirVector axis, double angle) {
        rotate(angle, (float) axis.x(), (float) axis.y(), (float) axis.z());
    }

    default void translate(Vector v) {
        translate((float) v.x(), (float) v.y(), (float) v.z());
    }

    default void scale(float s) {
        scale(s, s, s);
    }

    /**
     * the transformation resulting from preTransformation will be applied before applying the current transformation
     * @param preTransformation some affine matrix
     */
    void multiplyAffine(Matrix4f preTransformation);
}
