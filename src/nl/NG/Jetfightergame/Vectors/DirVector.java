package nl.NG.Jetfightergame.Vectors;

import nl.NG.Jetfightergame.Engine.Settings;
import org.joml.Vector3f;

import java.util.Random;

/**
 * @author Geert van Ieperen
 * created on 1-5-2017.
 */
public class DirVector extends Vector {
    public static final DirVector O = new DirVector(0.0f, 0.0f, 0.0f, 0);
    public static final DirVector X = new DirVector(1.0f, 0.0f, 0.0f, 1);
    public static final DirVector Y = new DirVector(0.0f, 1.0f, 0.0f, 1);
    public static final DirVector Z = new DirVector(0.0f, 0.0f, 1.0f, 1);

    public DirVector(float x, float y, float z) {
        super(x, y, z);
    }

    public DirVector(float x, float y, float z, float length) {
        super(x, y, z, length);
    }

    public DirVector(Vector3f v) {
        super(v.x, v.y, v.z);
    }

    /**
     * create a vector that may point in any direction, might technically even be an O-vector (although chance is negligible)
     * @return a random vector with length < 1, with slightly more chance to point to the edges of a cube than to the axes
     */
    public static DirVector random() {
        Random r = Settings.random;
        return new DirVector(2 * r.nextFloat() - 1, 2 * r.nextFloat() - 1, 2 * r.nextFloat() - 1);
    }

    @Override
    public DirVector normalized() {
        final float length = this.length();
        return new DirVector(this.x / length, this.y / length, this.z / length, 1f);
    }

    @Override
    public DirVector mirrorX() {
        return new DirVector(-this.x, this.y, this.z);
    }

    @Override
    public DirVector mirrorY() {
        return new DirVector(this.x, -this.y, this.z);
    }

    @Override
    public DirVector mirrorZ() {
        return new DirVector(this.x, this.y, -this.z);
    }

    @Override
    public DirVector cross(Vector that) {
        return new DirVector(this.y * that.z - this.z * that.y, this.z * that.x - this.x * that.z, this.x * that.y - this.y * that.x);
    }

    @Override
    public DirVector add(Vector that) {
        return new DirVector(this.x + that.x, this.y + that.y, this.z + that.z);
    }

    @Override
    public DirVector subtract(Vector that) {
        return new DirVector(this.x - that.x, this.y - that.y, this.z - that.z);
    }

    @Override
    public DirVector scale(float scalar) {
        return new DirVector(scalar * this.x, scalar * this.y, scalar * this.z);
    }

    @Override
    public DirVector middleTo(Vector that) {
        return new DirVector(this.x + 0.5f * (that.x - this.x), this.y + 0.5f * (that.y - this.y), this.z + 0.5f * (that.z - this.z));
    }

    @Override
    public DirVector to(Vector that) {
        return new DirVector(that.x - this.x, that.y - this.y, that.z - this.z);
    }

    @Override
    public PosVector toPosVector() {
        return new PosVector(x, y, z);
    }

    @Override
    public DirVector toDirVector() {
        return this;
    }

    /**
     * sets the length of this vector to {@code newLength}
     * also accepts negative length (result will point to the other side)
     * @param newLength the new length of this vector
     * @return the result of {@code this.normalized().scale(newlength)}
     * or a zero vector (0, 0, 0) if this is a zero-vector
     */
    public DirVector reduceTo(float newLength) {
        if (isNotScalable()) return this;
        final float factor = newLength / length();
        return new DirVector(this.x * factor, this.y * factor, this.z * factor, Math.abs(newLength));
    }
}
