package nl.NG.Jetfightergame.Tools.Vectors;

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
     * equals (this + (1/2)*(that - this)), or (this.interpolateTo(that, 0.5f))
     */
    public PosVector middleTo(Vector that) {
        return interpolateTo(that, 0.5f);
    }

    @Override
    public PosVector interpolateTo(Vector that, float scalar){
        final float x = this.x + ((that.x - this.x) * scalar);
        final float y = this.y + ((that.y - this.y) * scalar);
        final float z = this.z + ((that.z - this.z) * scalar);
        return new PosVector(x, y, z);
    }

    @Override
    public PosVector toPosVector() {
        return this;
    }
}
