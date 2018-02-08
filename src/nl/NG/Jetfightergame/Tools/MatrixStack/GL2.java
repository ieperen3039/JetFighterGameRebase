package nl.NG.Jetfightergame.Tools.MatrixStack;

import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 15-11-2017.
 */
public interface GL2 extends MatrixStack {

    void draw(Renderable object);

    void setLight(DirVector dir, Color4f lightColor);

    void setLight(PosVector pos, Color4f lightColor);

    void setMaterial(Material material, Color4f color);

    default void setMaterial(Material material){
        setMaterial(material, Color4f.WHITE);
    }

    /**
     * Objects should call GPU calls only in their render method. this render method may only be called by a GL2 object,
     * to prevent drawing calls while the GPU is not initialized. For this reason, the Painter constructor is protected.
     */
    class Painter {
        protected Painter(){}
    }
}
