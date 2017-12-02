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
public class GouraudShader extends ShaderProgram {

    public GouraudShader() throws IOException {
        super(
                "res/shaders/Gouraud/vertex.vert",
                "res/shaders/Gouraud/fragment.frag"
        );

        // Create the Material uniform
        createMaterialUniform();

        // Create the lighting uniforms
        createUniform("ambientLight");
        createPointLightsUniform(MAX_POINT_LIGHTS);
    }

    public void setAmbientLight(Color4f ambientLight) {
        setUniform("ambientLight", ambientLight);
    }

    /**
     * Create an uniform for a pointslight array.
     *
     * @param size The size of the array.
     * @throws ShaderException If an error while fetching the memory location.
     */
    private void createPointLightsUniform(int size) throws ShaderException {
        for (int i = 0; i < size; i++) {
            createUniform(("pointLights" + "[" + i + "]") + ".color");
            createUniform(("pointLights" + "[" + i + "]") + ".mPosition");
            createUniform(("pointLights" + "[" + i + "]") + ".intensity");
        }
    }

    /**
     * Create the uniforms required for a Material
     *
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    private void createMaterialUniform() throws ShaderException {
        createUniform("material.ambient");
        createUniform("material.diffuse");
        createUniform("material.specular");
        createUniform("material.reflectance");
    }

    @Override
    public void setPointLight(int lightNumber, Vector3f mvPosition, Color4f color) {
         setUniform("pointLights[" + lightNumber + "]", mvPosition, color);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void setMaterial(Material material, Color4f color) {
        float[] materialColor = material.mixWith(color);
        setUniform4f("material.ambient", materialColor);
        setUniform4f("material.diffuse", materialColor);
        setUniform4f("material.specular", material.specular);
        setUniform("material.reflectance", material.shininess);
    }
}
