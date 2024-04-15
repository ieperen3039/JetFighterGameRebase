package nl.NG.Jetfightergame.Rendering.Shaders;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.joml.Vector3f;

import java.io.IOException;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class HeightShader extends AbstractShader {

    public HeightShader() throws ShaderException, IOException {
        super(shaders.getPath("Phong", "vertex.vert"), shaders.getPath("Height", "fragment.frag"));
        createUniform("cameraPosition");
    }

    @Override
    public void setPointLight(int lightNumber, Vector3f mPosition, Color4f color) {

    }

    public void setCameraPosition(Vector3f mPosition){
        setUniform("cameraPosition", mPosition);
    }

    @Override
    public void setMaterial(Color4f diffuse, Color4f specular, float reflectance) {

    }
}
