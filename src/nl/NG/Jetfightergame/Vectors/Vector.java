package nl.NG.Jetfightergame.Vectors;

import nl.NG.Jetfightergame.Engine.Settings;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Locale;

/**
 * @author Geert van Ieperen
 * by local definition: z is up.
 * openGL normally does not agree on this, but the effect is the same anyhow
 */

public abstract class Vector extends Vector3f{

    private static final float ROUNDINGERROR = 1E-6F;

    public Vector(Vector3fc source){
        super(source);
    }

    public Vector(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector() {
    }

    public static boolean almostZero(float number) {
        return (((number + ROUNDINGERROR) >= 0.0f) && ((number - ROUNDINGERROR) <= 0.0f));
    }

    public DirVector normalize(DirVector dest) {
        super.normalize(dest);
        return dest;
    }

    /**
     * create a vector that may point in any direction, might technically even be an O-vector (although chance is negligible)
     * @return a random vector with length < 1, with slightly more chance to point to the edges of a cube than to the axes
     */
    public static DirVector random() {
        return new DirVector(
                (2 * Settings.random.nextFloat()) - 1,
                (2 * Settings.random.nextFloat()) - 1,
                (2 * Settings.random.nextFloat()) - 1
        );
    }

    public boolean isScalable(){
        return !Float.isNaN(x) && !Float.isNaN(y) && !Float.isNaN(z) && !((x == 0) && (y == 0) && (z == 0));
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

    public String toExactString(){
        return super.toString();
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

