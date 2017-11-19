package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.GameObjects.Structures.Renderable;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 15-11-2017.
 */
public interface GL2 {

    public static int GL_PROJECTION = GL11.GL_PROJECTION;
    public static int GL_MODELVIEW = GL11.GL_MODELVIEW;

    /** TODO integrate with matrix
     * rotates the axis frame such that the z-axis points from source to target vector,
     * and translates the system to source
     * if (target == source) the axis will not turn
     * @param source the vector where the axis will have its orgin upon returning
     * @param target the vector in which direction the z-axis will point upon returning
     */
    default void pointFromTo(PosVector source, PosVector target) {
        if (target.equals(source)) return;
        DirVector parallelVector = source.to(target)
                .normalized();

        DirVector M = DirVector.Z.cross(parallelVector);
        double angle = Math.acos(DirVector.Z.dot(parallelVector));// in Radians

        translate(source);
        rotate(M, angle);
    }

    void draw(Renderable object);

    @Deprecated
    void setColor(double red, double green, double blue);

    void setFustrum(int width, int height);

    void setLight(int lightNumber, DirVector dir, Color lightColor);

    void setLight(int lightNumber, PosVector pos, Color lightColor);

    // TODO add transparency
    void setMaterial(Material material);

    void rotate(double angle, float x, float y, float z);

    void translate(float x, float y, float z);

    void scale(float x, float y, float z);

    PosVector getPosition(PosVector p);

    DirVector getDirection(DirVector v);

    void pushMatrix();

    void popMatrix();

    @Deprecated
    default void setColor(Color color) {
        setColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    default void setColor(int red, int green, int blue) {
        setColor(red/255f, green/255f, blue/255f);
    }

    default void rotate(DirVector axis, double angle) {
        rotate(angle, (float) axis.x(), (float) axis.y(), (float) axis.z());
    }

    default void translate(Vector v) {
        translate((float) v.x(), (float) v.y(), (float) v.z());
    }

    default void scale(float s) {
        scale(s, s, s);
    }

    void clearColor();

    /**
     * the transformation resulting from preTransformation will be applied before applying the current transformation
     * @param preTransformation
     */
    void multiplyAffine(Matrix4f preTransformation);

    void setCamera(Camera activeCamera);

    /**
     * Objects should call GPU calls only in their render method. this render method may only be called by a GL2 object,
     * to prevent drawing calls while the GPU is not initialized. For this reason, the Painter constructor is protected.
     */
    class Painter {
        protected Painter(){}
    }
}
