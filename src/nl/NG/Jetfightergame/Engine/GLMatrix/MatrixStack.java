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
    /** TODO integrate with matrix
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

    void rotate(double angle, float x, float y, float z);

    void translate(float x, float y, float z);

    void scale(float x, float y, float z);

    PosVector getPosition(PosVector p);

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
     * @param preTransformation
     */
    void multiplyAffine(Matrix4f preTransformation);
}
