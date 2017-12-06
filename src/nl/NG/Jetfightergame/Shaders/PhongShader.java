package nl.NG.Jetfightergame.Shaders;

import nl.NG.Jetfightergame.Vectors.Color4f;
import org.joml.Vector3f;

import java.io.IOException;

import static nl.NG.Jetfightergame.Engine.Settings.MAX_POINT_LIGHTS;

/**
 * @author Geert van Ieperen
 *         created on 2-12-2017.
 */
@SuppressWarnings("Duplicates")
public class PhongShader extends ShaderProgram {

    public PhongShader() throws ShaderException, IOException {
        super(
                "res/shaders/Phong/vertex.vert", 
                "res/shaders/Phong/fragment.frag"
        );

        // Create the Material uniform
        createUniform("material.ambient");
        createUniform("material.diffuse");
        createUniform("material.specular");
        createUniform("material.reflectance");

        // Create the lighting uniforms
        createUniform("specularPower");
        createUniform("ambientLight");
        createUniform("cameraPosition");
        createPointLightsUniform(MAX_POINT_LIGHTS);

        // Create uniform for special lighting conditions for background elements
        createUniform("blackAsAlpha");
        createUniform("shadowed");
    }

    public void setSpecular(float power) {
        setUniform("specularPower", power);
    }

    public void setBlack(boolean asAlpha) {
        setUniform("blackAsAlpha", asAlpha);
    }

    public void setShadowed(boolean isShadowed) {
        setUniform("shadowed", isShadowed);
    }

    public void setAmbientLight(Color4f ambientLight) {
        setUniform("ambientLight", ambientLight);
    }

    public void setCameraPosition(Vector3f mPosition){
        setUniform("cameraPosition", mPosition);
    }

    /**
     * Create an uniform for a pointslight array.
     *
     * @param size The size of the array.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    private void createPointLightsUniform(int size) throws ShaderException {
        for (int i = 0; i < size; i++) {
            createUniform(("pointLights" + "[" + i + "]") + ".color");
            createUniform(("pointLights" + "[" + i + "]") + ".mPosition");
            createUniform(("pointLights" + "[" + i + "]") + ".intensity");
        }
    }

    @Override
    public void setPointLight(int lightNumber, Vector3f mPosition, Color4f color) {
        setPointLightUniform("pointLights[" + lightNumber + "]", mPosition, color);
    }

    @Override
    public void setMaterial(Material material, Color4f color) {
        float[] materialColor = material.mixWith(color);
        setUniform4f("material.ambient", materialColor);
        setUniform4f("material.diffuse", materialColor);
        setUniform4f("material.specular", material.specular);
        setUniform("material.reflectance", material.shininess);
    }
}
