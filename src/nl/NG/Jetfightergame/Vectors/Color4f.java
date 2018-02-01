package nl.NG.Jetfightergame.Vectors;

import org.joml.Vector3f;
import org.joml.Vector4f;

import static java.lang.Math.min;

/**
 * @author Geert van Ieperen
 *         created on 21-11-2017.
 */
public class Color4f {
    public static final Color4f BLACK = new Color4f(0, 0, 0);
    public static final Color4f GREY = new Color4f(0.5f, 0.5f, 0.5f);
    public static final Color4f LIGHT_GREY = new Color4f(0.8f, 0.8f, 0.8f);
    public static final Color4f WHITE = new Color4f(1, 1, 1);
    public static final Color4f RED = new Color4f(1, 0, 0);
    public static final Color4f GREEN = new Color4f(0, 1, 0);
    public static final Color4f BLUE = new Color4f(0, 0, 1);
    public static final Color4f YELLOW = new Color4f(1, 1, 0);
    public static final Color4f MAGENTA = new Color4f(1, 0, 1);
    public static final Color4f CYAN = new Color4f(0, 1, 1);
    public static final Color4f ORANGE = new Color4f(1, 0.5f, 0);

    /** a color with alpha == 0, thus (technically) not visible */
    public static final Color4f INVISIBLE = new Color4f(0, 0, 0, 0);
    public static final Color4f TRANSPARENT_GREY = new Color4f(0.5f, 0.5f, 0.5f, 0.5f);

    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;

    public Color4f(java.awt.Color target){
        this(target.getRed(), target.getGreen(), target.getBlue(), target.getAlpha());
    }

    public Color4f(float red, float green, float blue, float alpha) {
        this.red = min(red, 1f);
        this.blue = min(blue, 1f);
        this.green = min(green, 1f);
        this.alpha = min(alpha, 1f);
    }

    /**
     * @param vec a color described as a vector with values [0, 1]
     */
    public Color4f(Vector4f vec){
        this(vec.x, vec.y, vec.z, vec.w);
    }

    /**
     * @param color a color described as a vector with values [0, 1]
     * @param intensity the alpha value
     */
    public Color4f(Vector3f color, float intensity) {
        this(color.x, color.y, color.z, intensity);
    }

    public Color4f(float red, float green, float blue) {
        this(red, green, blue, 1f);
    }

    /**
     * create a color using integer arguments [0 - 255]
     * with 0 = none and 255 = max
     */
    public Color4f(int ired, int igreen, int iblue, int ialpha){
        red = ired/255f;
        green = igreen/255f;
        blue = iblue/255f;
        alpha = ialpha/255f;
    }

    /**
     * flat add this color with the given other color
     * @param other another color
     * @return each color aspect added to the other, with alpha inverse multiplied
     * @see #overlay(Color4f)
     */
    public Color4f add(Color4f other){
        return new Color4f(
                cap(red + other.red),
                cap(green + other.green),
                cap(blue + other.blue),
                inverseMul(alpha, other.alpha)
        );
    }

    /**
     * adds color as combined light, using inverse multiplication
     * @param other another color
     * @return inverse multiplication of every color aspect including alpha
     */
    public Color4f overlay(Color4f other){
        return new Color4f(
                inverseMul(red, other.red),
                inverseMul(green, other.green),
                inverseMul(blue, other.blue),
                inverseMul(alpha, other.alpha)
        );
    }

    /** culls parameter to [0, 1] */
    private float cap(float input) {
         return Math.min(Math.max(input, 0f), 1f);
    }

    /**
     * intensifies this color by adding a white light to it.
     * does not affect alpha, and is not the inverse of {@link #darken(float)}
     * @param scalar a factor in [0, 1], where 0 gives no change, and 1 makes this color effectively white
     * @return the new color, brighted up
     * @see #darken(float)
     */
    public Color4f intensify(float scalar){
        return overlay(new Color4f(scalar, scalar, scalar, alpha));
    }

    /**
     * darken this color by linearly fading it to black.
     * does not affect alpha, and is not the inverse of {@link #intensify(float)}
     * @param scalar a factor in [0, 1], where 0 gives no change, and 1 makes this color effectively black
     * @return the new color, darkened up
     * @see #intensify(float)
     */
    public Color4f darken(float scalar){
        return new Color4f(
                red * (1-scalar),
                green * (1-scalar),
                blue * (1-scalar),
                alpha
        );
    }

    /**
     * @return 1 - ((1 - alpha) * (1 - beta))
     */
    private float inverseMul(float alpha, float beta) {
        return 1f - ((1f - alpha) * (1f - beta));
    }

    /**
     * @return the vector that would represent this color, when the color is multiplied with the alpha value
     */
    public Vector3f toVector3f() {
        return new Vector3f(red*alpha, green*alpha, blue*alpha);
    }

    public Vector4f toVector4f() {
        return new Vector4f(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Color4f other = (Color4f) o;
        if (other.red != red) return false;
        if (other.green != green) return false;
        if (other.blue != blue) return false;
        return (other.alpha == alpha);
    }

    @Override
    public int hashCode() {
        int result = ((red != +0.0f) ? Float.floatToIntBits(red) : 0);
        result = (31 * result) + ((green != +0.0f) ? Float.floatToIntBits(green) : 0);
        result = (31 * result) + ((blue != +0.0f) ? Float.floatToIntBits(blue) : 0);
        result = (31 * result) + ((alpha != +0.0f) ? Float.floatToIntBits(alpha) : 0);
        return result;
    }
}
