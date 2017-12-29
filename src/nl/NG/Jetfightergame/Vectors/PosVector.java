package nl.NG.Jetfightergame.Vectors;

import org.joml.Vector3f;

/**
 * Created by Geert van Ieperen on 1-5-2017.
 */
public class PosVector extends Vector {

    public PosVector() {

    }

    public static PosVector zeroVector() {
        return new PosVector(0.0f, 0.0f, 0.0f);
    }

    public static PosVector zVector() {
        return new PosVector(0.0f, 0.0f, 1.0f);
    }

    public PosVector(float x, float y, float z) {
        super(x, y, z);
    }

    public PosVector(Vector3f v) {
        super(v.x, v.y, v.z);
    }


    public PosVector mirrorX(PosVector dest) {
        dest.set(-this.x, this.y, this.z);
        return dest;
    }


    public PosVector mirrorY(PosVector dest) {
        dest.set(this.x, -this.y, this.z);
        return dest;
    }


    public PosVector mirrorZ(PosVector dest) {
        dest.set(this.x, this.y, -this.z);
        return dest;
    }


    public PosVector cross(Vector that, PosVector dest) {
        super.cross(that, dest);
        return dest;
    }


    public PosVector add(Vector that, PosVector dest) {
        super.add(that, dest);
        return dest;
    }


    public PosVector sub(Vector that, PosVector dest) {
        super.sub(that, dest);
        return dest;
    }

    /**
     * multiplies the length of this vector with scalar
     * @param scalar multiplication factor.
     * @param dest
     * @return new vector with length equal to {@code this.length() * scalar}
     */
    public Vector scale(float scalar, PosVector dest) {
        super.mul(scalar, dest);
        return dest;
    }

    /**
     * @return vector to the middle of this vector and given vector
     * equals (this + (1/2)*(that - this))
     */
    public PosVector middleTo(Vector that, PosVector dest) {
        dest.set(this.x + 0.5f * (that.x - this.x), this.y + 0.5f * (that.y - this.y), this.z + 0.5f * (that.z - this.z));
        return dest;
    }

    @Override
    public PosVector toPosVector() {
        return this;
    }
}
