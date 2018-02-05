package nl.NG.Jetfightergame.Vectors;

import org.joml.Vector3f;

/**
 * @author Geert van Ieperen
 * created on 1-5-2017.
 */
public class DirVector extends Vector {

    public static DirVector zeroVector() {
        return new DirVector(0.0f, 0.0f, 0.0f);
    }

    public static DirVector xVector() {
        return new DirVector(1.0f, 0.0f, 0.0f);
    }

    public static DirVector yVector() {
        return new DirVector(0.0f, 1.0f, 0.0f);
    }

    public static DirVector zVector() {
        return new DirVector(0.0f, 0.0f, 1.0f);
    }

    public DirVector(){
        super();
    }

    public DirVector(float x, float y, float z) {
        super(x, y, z);
    }

    public DirVector(Vector3f v) {
        super(v);
    }

    public DirVector mirrorX(DirVector dest) {
        dest.set(-this.x, this.y, this.z);
        return dest;
    }

    public DirVector mirrorY(DirVector dest) {
        dest.set(this.x, -this.y, this.z);
        return dest;
    }

    public DirVector mirrorZ(DirVector dest) {
        dest.set(this.x, this.y, -this.z);
        return dest;
    }

    public DirVector cross(DirVector that, DirVector dest) {
        super.cross(that, dest);
        return dest;
    }

    public DirVector add(Vector that, DirVector dest) {
        super.add(that, dest);
        return dest;
    }

    public DirVector sub(Vector that, DirVector dest) {
        super.sub(that, dest);
        return dest;
    }

    /**
     * multiplies the length of this vector with scalar
     * @param scalar multiplication factor.
     * @param dest will hold the result
     * @return new vector with length equal to {@code this.length() * scalar}
     */
    public DirVector scale(float scalar, DirVector dest) {
        super.mul(scalar, dest);
        return dest;
    }

    /**
     * sets the length of this vector to {@code newLength},
     * also accepts negative length (result will point to the other side)
     * @param newLength the new length of this vector
     * @param dest the target where the result will be stored in
     * @return the result of {@code this.normalized().scale(newlength)}
     * or a zero vector (0, 0, 0) if this is a zero-vector
     */
    public DirVector reducedTo(float newLength, DirVector dest) {
        if (!isScalable()) {
            dest.zero();
            return dest;
        }
        this.mul(newLength / length(), dest);
        return dest;
    }

    public DirVector negate(DirVector dest){
        super.negate(dest);
        return dest;
    }

    /**
     * @return vector to the middle of this vector and given vector
     * equals (this + (1/2)*(that - this))
     */
    public Vector middleTo(Vector that, Vector dest) {
        dest.set(this.x + (0.5f * (that.x - this.x)), this.y + (0.5f * (that.y - this.y)), this.z + (0.5f * (that.z - this.z)));
        return dest;
    }
}
