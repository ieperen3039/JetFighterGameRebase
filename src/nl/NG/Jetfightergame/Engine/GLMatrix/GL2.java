package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Geert van Ieperen
 *         created on 15-11-2017.
 */
public interface GL2 extends MatrixStack {

    public static int GL_PROJECTION = GL11.GL_PROJECTION;
    public static int GL_MODELVIEW = GL11.GL_MODELVIEW;

    void draw(Renderable object);

    void setFustrum(int width, int height);

    void setLight(int lightNumber, DirVector dir, Color lightColor);

    void setLight(int lightNumber, PosVector pos, Color lightColor);

    // TODO add transparency
    void setMaterial(Material material);

    void clearColor();

    void setCamera(Camera activeCamera);

    /**
     * Objects should call GPU calls only in their render method. this render method may only be called by a GL2 object,
     * to prevent drawing calls while the GPU is not initialized. For this reason, the Painter constructor is protected.
     */
    class Painter {
        protected Painter(){}
    }
}
