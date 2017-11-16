package nl.NG.Jetfightergame.Vectors;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import org.joml.Vector3f;

import java.util.Locale;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * @author Geert van Ieperen
 * by local definition: z is up.
 * openGL normally does not agree on this, but the effect is the same anyhow
 */

public abstract class Vector {

    protected static final double roundingError = 4.44E-16;
    protected final double x;
    protected final double y;
    protected final double z;

    /**
     * the length of this vector which is 0 until queried
     */
    private double length = -1;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(double x, double y, double z, double length) {
        this(x, y, z);
        this.length = length;
    }

    public static boolean almostZero(double number) {
        return (number + 2 * roundingError >= 0.0 && number - 2 * roundingError <= 0.0);
    }

    /**
     * returns a translation matrix resulting from rotating angle radians over vector (x, y, z)
     * in much the same way as {@link GL2#rotate(double, double, double, double)}
     *
     * @param angle in RADIANS
     * @return a 3x3 matrix as 9-element array.
     */
    public static double[] getRotationMatrix(double x, double y, double z, double angle) {
        double Mcos = (1 - cos(angle));
        return new double[]{
                (x * x * Mcos + cos(angle)),
                (x * y * Mcos - z * sin(angle)),
                (x * z * Mcos + y * sin(angle)),
                (y * x * Mcos + z * sin(angle)),
                (y * y * Mcos + cos(angle)),
                (y * z * Mcos - x * sin(angle)),
                (z * x * Mcos - y * sin(angle)),
                (z * y * Mcos + x * sin(angle)),
                (z * z * Mcos + cos(angle))
        };
    }

    public static double[] getRotationMatrix(Vector v, double angle){
        return getRotationMatrix(v.x, v.y, v.z, angle);
    }

    public Vector multiplyMatrix(double[] mat){
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
     * @return {@link #getRotationMatrix(double, double, double, double)} on turnvector,
     * then {@link #multiplyMatrix(double[])} on the resulting 3x3 matrix.
     */
    public Vector rotateVector(DirVector turnVector, float angle){
        double[] mat = getRotationMatrix(turnVector, angle);

        return multiplyMatrix(mat);
    }

    public double length() {
        if (length < 0) length = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
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
    public boolean isScalable(){
        return !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z) && !(x == 0 && y == 0 && z == 0);
    }

    public double dot(Vector that) {
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
    public abstract Vector scale(double scalar);

    /**
     * @returns vector to the middle of this vector and given vector
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

    public String toExactString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public abstract PosVector toPosVector();

    public abstract DirVector toDirVector();

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector that = (Vector) o;
        return (this.x == that.x) && (this.y == that.y) && (this.z == that.z);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Vector3f toVector3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }
}

