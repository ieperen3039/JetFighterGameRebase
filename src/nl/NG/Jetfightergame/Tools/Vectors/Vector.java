package nl.NG.Jetfightergame.Tools.Vectors;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Locale;

import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

/**
 * @author Geert van Ieperen
 * by local definition: z is up.
 * openGL normally does not agree on this, but the effect is the same anyhow
 */

public abstract class Vector extends Vector3f implements Serializable {

    public Vector(Vector3fc source){
        super(source);
    }

    public Vector(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector() {
    }

    public DirVector normalize(DirVector dest) {
        super.normalize(dest);
        return dest;
    }

    /**
     * create a vector that may point in any direction, might technically even be an O-vector (although chance is negligible)
     * @return a random vector with length < 1, with slightly more chance to point to the edges of a cube than to the axes
     * @see #randomOrb() for a better random
     */
    public static DirVector random() {
        return new DirVector(
                (2 * ServerSettings.random.nextFloat()) - 1,
                (2 * ServerSettings.random.nextFloat()) - 1,
                (2 * ServerSettings.random.nextFloat()) - 1
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

    /**
     * interpolates or extrapolates this vector to the given vector linearly
     * @param that a target vector
     * @param scalar [0, 1] for interpolation, otherwise it is extrapolation. 0 returns {@code this}, 1 returns {@code that}
     * @return a new vector with the interpolated value
     */
    public abstract Vector interpolateTo(Vector that, float scalar);

    public PosVector toPosVector() {
        return new PosVector(x, y, z);
    }

    public DirVector toDirVector() {
        return new DirVector(x, y, z);
    }

    public void rotateAxis(Vector v, float angle, DirVector dest) {
        rotateAxis(angle, v.x, v.y, v.z, dest);
    }

    /**
     * @return a vector with length < 1 that is universally distributed. Would form a solid sphere when created points
     */
    public static DirVector randomOrb() {
        float phi = ServerSettings.random.nextFloat() * 6.2832f;
        float costheta = (ServerSettings.random.nextFloat() * 2) - 1;

        float theta = (float) Math.acos(costheta);
        float r = (float) Math.cbrt(ServerSettings.random.nextFloat());

        float x = (float) (r * sin(theta) * cos(phi));
        float y = (float) (r * sin(theta) * sin(phi));
        float z = (float) (r * cos(theta));
        return new DirVector(x, y, z);
    }

    public FloatBuffer toFloatBuffer() {
        return BufferUtils.createFloatBuffer(3).put(new float[]{x, y, z});
    }
}

