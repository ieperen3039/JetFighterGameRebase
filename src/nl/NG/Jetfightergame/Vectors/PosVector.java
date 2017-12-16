package nl.NG.Jetfightergame.Vectors;

import org.joml.Vector3f;

/**
 * Created by Geert van Ieperen on 1-5-2017.
 */
public class PosVector extends Vector {

    public static final PosVector O = new PosVector(0.0f, 0.0f, 0.0f, 0.0f);
    public static final PosVector Z = new PosVector(0.0f, 0.0f, 1.0f, 1.0f);

    public PosVector(float x, float y, float z) {
        super(x, y, z);
    }

    public PosVector(float x, float y, float z, float length) {
        super(x, y, z, length);
    }

    public PosVector(Vector3f v) {
        super(v.x, v.y, v.z);
    }

    @Override
    public DirVector normalized() {
        final float length = this.length();
        return new DirVector(this.x / length, this.y / length, this.z / length, 1f);
    }

    @Override
    public PosVector mirrorX() {
        return new PosVector(-this.x, this.y, this.z);
    }

    @Override
    public PosVector mirrorY() {
        return new PosVector(this.x, -this.y, this.z);
    }

    @Override
    public PosVector mirrorZ() {
        return new PosVector(this.x, this.y, -this.z);
    }

    @Override
    public PosVector cross(Vector that) {
        return new PosVector(this.y * that.z - this.z * that.y, this.z * that.x - this.x * that.z, this.x * that.y - this.y * that.x);
    }

    @Override
    public PosVector add(Vector that) {
        return new PosVector(this.x + that.x, this.y + that.y, this.z + that.z);
    }

    @Override
    public PosVector subtract(Vector that) {
        return new PosVector(this.x - that.x, this.y - that.y, this.z - that.z);
    }

    @Override
    public Vector scale(float scalar) {
        return new PosVector(scalar * this.x, scalar * this.y, scalar * this.z);
    }

    @Override
    public PosVector middleTo(Vector that) {
        return new PosVector(this.x + 0.5f * (that.x - this.x), this.y + 0.5f * (that.y - this.y), this.z + 0.5f * (that.z - this.z));
    }

    @Override
    public DirVector to(Vector that) {
        return new DirVector(that.x - this.x, that.y - this.y, that.z - this.z);
    }

    @Override
    public PosVector toPosVector() {
        return this;
    }

    @Override
    public DirVector toDirVector() {
        return new DirVector(x, y, z);
    }
}
