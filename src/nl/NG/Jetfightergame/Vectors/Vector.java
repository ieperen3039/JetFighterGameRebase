package nl.NG.Jetfightergame.Vectors;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Locale;

import static java.lang.Math.sin;

/**
 * @author Geert van Ieperen
 * by local definition: z is up.
 * openGL normally does not agree on this, but the effect is the same anyhow
 */

public abstract class Vector extends Vector3f{

    private static final double roundingError = 4.44E-16;

    /**
     * the length of this vector which is 0 until queried
     */
    private float length = -1;

    public Vector(Vector3fc source){
        super(source);
    }

    public Vector(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector(float x, float y, float z, float length) {
        super(x, y, z);
        this.length = length;
    }

    public static boolean almostZero(double number) {
        return (number + 2 * roundingError >= 0.0 && number - 2 * roundingError <= 0.0);
    }

    /**
     * returns a translation matrix resulting from rotating angle radians over vector (x, y, z)
     * in much the same way as {@link MatrixStack#rotate(float, float, float, float)}
     *
     * @param x
     * @param angle in RADIANS
     * @return a 3x3 matrix as 9-element array.
     */
    public static float[] getRotationMatrix(float x, float z, float y, float angle) {
        float Mcos = (1 - (float) Math.cos(angle));
        return new float[]{
                (x * x * Mcos + (float) Math.cos(angle)),
                (x * y * Mcos - z * (float) sin(angle)),
                (x * z * Mcos + y * (float) sin(angle)),
                (y * x * Mcos + z * (float) sin(angle)),
                (y * y * Mcos + (float) Math.cos(angle)),
                (y * z * Mcos - x * (float) sin(angle)),
                (z * x * Mcos - y * (float) sin(angle)),
                (z * y * Mcos + x * (float) sin(angle)),
                (z * z * Mcos + (float) Math.cos(angle))
        };
    }

    public static float[] getRotationMatrix(Vector v, float angle){
        return getRotationMatrix(v.x, v.z, v.y, angle);
    }

    public Vector multiplyMatrix(float[] mat){
        return new DirVector(
                x * mat[0] + y * mat[1] + z * mat[2],
                x * mat[3] + y * mat[4] + z * mat[5],
                x * mat[6] + y * mat[7] + z * mat[8]
        );
    }

    /**
     * turns a vector along an axis.
     * If multiple vectors must be turned along the same axis, better reuse the matrix
     * @param turnVector the vector that is orthogonal to the turned plane
     * @param angle the angle in RADIANS that has to be turned
     * @return {@link #getRotationMatrix(float, float, float, float)} on turnvector,
     * then {@link #multiplyMatrix(float[])} on the resulting 3x3 matrix.
     */
    public Vector rotateVector(DirVector turnVector, float angle){
        float[] mat = getRotationMatrix(turnVector, angle);

        return multiplyMatrix(mat);
    }

    public float length() {
        if (length < 0) length = super.length();
        return length;
    }

    /**
     * @return length of this vector
     * @throws ArithmeticException when applied to a zero-vector
     */
    public abstract Vector normalized();

    /**
     * mirrors this vector in the ZY-plane
     */
    public abstract Vector mirrorX();

    /**
     * mirrors this vector in the ZX-plane
     */
    public abstract Vector mirrorY();

    /**
     * mirrors this vector in the XY-plane
     */
    public abstract Vector mirrorZ();

    /**
     * @return whether all coordinates are not NaN, and this is not a zero-vector
     */
    public boolean isNotScalable(){
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || (x == 0 && y == 0 && z == 0);
    }

    public float dot(Vector that) {
        return (this.x * that.x) + (this.y * that.y) + (this.z * that.z);
    }

    public abstract Vector cross(Vector that);

    public abstract Vector add(Vector that);

    public abstract Vector subtract(Vector that);

    /**
     * multiplies the length of this vector with scalar
     * @param scalar multiplication factor.
     * @return new vector with length equal to {@code this.length() * scalar}
     */
    public abstract Vector scale(float scalar);

    /**
     * @return vector to the middle of this vector and given vector
     * equals (this + (1/2)*(that - this))
     */
    public abstract Vector middleTo(Vector that);

    /**
     * returns the vector from this Vector to Vector that
     */
    public abstract Vector to(Vector that);

    public String toString() {
        return String.format(Locale.US, "(%1.03f, %1.03f, %1.03f)", this.x, this.y, this.z);
    }

    public abstract PosVector toPosVector();

    public abstract DirVector toDirVector();
}

