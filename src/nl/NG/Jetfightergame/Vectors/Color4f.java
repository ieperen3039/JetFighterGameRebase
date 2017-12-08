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

    /** a color with alpha == 0, thus (technically) not visible */
    public static final Color4f INVISIBLE = new Color4f(0, 0, 0, 0);
    public static final Color4f TRANSPARENT_GREY = new Color4f(0.5f, 0.5f, 0.5f, 0.5f);

    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;

    public Color4f(java.awt.Color target){
        red = target.getRed()/255f;
        green = target.getGreen()/255f;
        blue = target.getBlue()/255f;
        alpha = target.getAlpha()/255f;
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
     * flat add this color with the given other color
     * @param other another color
     * @return each color aspect added to the other, with alpha inverse multiplied
     * @see #overlay(Color4f)
     */
    public Color4f add(Color4f other){
        return new Color4f(
                red + other.red,
                green + other.green,
                blue + other.blue,
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
                red * scalar,
                green * scalar,
                blue * scalar,
                alpha
        );
    }

    /**
     * @return 1 - ((1 - alpha) * (1 - beta))
     */
    private float inverseMul(float alpha, float beta) {
        return 1f - ((1f - alpha) * (1f - beta));
    }

    public Vector3f toVector3f() {
        return new Vector3f(red, green, blue);
    }

    public Vector4f toVector4f() {
        return new Vector4f(red, green, blue, alpha);
    }
}
