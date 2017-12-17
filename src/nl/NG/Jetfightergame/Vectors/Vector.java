package nl.NG.Jetfightergame.Vectors;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.Settings;
import org.joml.Matrix3f;
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

    public Vector(Vector3fc source){
        super(source);
    }

    public Vector(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector() {
    }

    @Override
    public Vector3f set(float x, float y, float z) {
        return super.set(x, y, z);
    }

    public static boolean almostZero(double number) {
        return (number + 2 * roundingError >= 0.0 && number - 2 * roundingError <= 0.0);
    }

    /**
     * returns a translation matrix resulting from rotating angle radians over vector (x, y, z)
     * in much the same way as {@link MatrixStack#rotate(float, float, float, float)}
     *
     * @param angle in RADIANS
     * @return a 3x3 matrix as 9-element array.
     */
    public static Matrix3f getRotationMatrix(float x, float z, float y, float angle) {
        float Mcos = (1 - (float) Math.cos(angle));
        return new Matrix3f(
                (x * x * Mcos + (float) Math.cos(angle)),
                (x * y * Mcos - z * (float) sin(angle)),
                (x * z * Mcos + y * (float) sin(angle)),
                (y * x * Mcos + z * (float) sin(angle)),
                (y * y * Mcos + (float) Math.cos(angle)),
                (y * z * Mcos - x * (float) sin(angle)),
                (z * x * Mcos - y * (float) sin(angle)),
                (z * y * Mcos + x * (float) sin(angle)),
                (z * z * Mcos + (float) Math.cos(angle))
        );
    }

    public static Matrix3f getRotationMatrix(Vector v, float angle){
        return getRotationMatrix(v.x, v.z, v.y, angle);
    }

    public DirVector normalized(DirVector dest) {
        normalize(dest);
        return dest;
    }

    /**
     * create a vector that may point in any direction, might technically even be an O-vector (although chance is negligible)
     * @return a random vector with length < 1, with slightly more chance to point to the edges of a cube than to the axes
     */
    public static DirVector random() {
        return new DirVector(
                2 * Settings.random.nextFloat() - 1,
                2 * Settings.random.nextFloat() - 1,
                2 * Settings.random.nextFloat() - 1
        );
    }

    /**
     * @return whether all coordinates are not NaN, and this is not a zero-vector
     */
    public boolean isNotScalable(){
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || (x == 0 && y == 0 && z == 0);
    }

    public float dot(Vector that) {
        return (this.x * that.x) + (this.y * that.y) + (this.z * that.z);
    }

    public DirVector to(Vector that, DirVector dest) {
        that.sub(this, dest);
        return dest;
    }

    public String toString() {
        return String.format(Locale.US, "(%1.03f, %1.03f, %1.03f)", this.x, this.y, this.z);
    }

    public PosVector toPosVector() {
        return new PosVector(x, y, z);
    }

    public DirVector toDirVector() {
        return new DirVector(x, y, z);
    }

    public void rotateAxis(Vector v, float angle, DirVector dest) {
        rotateAxis(angle, v.x, v.y, v.z, dest);
    }
}

