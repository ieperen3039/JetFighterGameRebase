package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 15-11-2017.
 */
public interface GL2 {

    public static int GL_PROJECTION = GL11.GL_PROJECTION;
    public static int GL_MODELVIEW = GL11.GL_MODELVIEW;

    void matrixMode(int matrix);

    void setColor(double red, double green, double blue);

    void setLight(int lightNumber, DirVector dir, Color lightColor);

    void setLight(int lightNumber, PosVector pos, Color lightColor);

    // TODO add transparency
    void setMaterial(Material material);

    void rotate(double angle, double x, double y, double z);

    void translate(double x, double y, double z);

    void scale(double x, double y, double z);

    PosVector getPosition(PosVector p);

    DirVector getDirection(DirVector v);

    void pushMatrix();

    void popMatrix();

    default void setColor(Color color) {
        setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    default void setColor(int red, int green, int blue) {
        setColor(red/255f, green/255f, blue/255f);
    }

    default void rotate(DirVector axis, double angle) {
        rotate(angle, axis.x(), axis.y(), axis.z());
    }

    default void translate(Vector v) {
        translate(v.x(), v.y(), v.z());
    }

    default void scale(double s) {
        scale(s, s, s);
    }

    void clearColor();
}
