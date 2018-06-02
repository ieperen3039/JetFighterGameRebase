package nl.NG.Jetfightergame.Rendering.Shaders;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class ShaderManager implements Manager<ShaderManager.ShaderImpl>, ShaderProgram {
    private ShaderProgram instance;

    public ShaderManager() throws IOException {
        instance = new PhongShader();
    }

    public enum ShaderImpl {
        phongShader,
        gouraudShader,
        heightShader
    }

    @Override
    public ShaderImpl[] implementations() {
        return ShaderImpl.values();
    }

    @Override
    public void switchTo(ShaderImpl implementation) {
        instance.cleanup();

        try {
            switch (implementation) {
                case gouraudShader:
                    instance = new GouraudShader();
                    break;
                case phongShader:
                    instance = new PhongShader();
                    break;
                case heightShader:
                    instance = new HeightShader();
                    break;
                default:
                    throw new UnsupportedOperationException("unknown enum: " + implementation);
            }
        } catch (Exception ex){
            ex.printStackTrace();
            try {
                instance = new GouraudShader();
            } catch (IOException error){
                throw new RuntimeException(error);
            }
        }
    }

    public void initShader(Camera activeCamera, Color4f ambientLight, float fog) {
        instance.bind();
        PosVector eye = activeCamera.getEye();

        if (instance instanceof PhongShader){
            PhongShader shader = (PhongShader) instance;
            shader.setSpecular(1f);
            shader.setAmbientLight(ambientLight);
            shader.setFog(fog);
            shader.setCameraPosition(eye);

        } else if (instance instanceof GouraudShader) {
            GouraudShader shader = (GouraudShader) instance;
            shader.setAmbientLight(ambientLight);
            shader.setFog(fog);
            shader.setCameraPosition(eye);

        } else if (instance instanceof HeightShader) {
            ((HeightShader) instance).setCameraPosition(eye);

        } else {
            String name = instance.getClass().getSimpleName();
            Logger.printSpamless(name, "loaded shader without advanced parameters: " + name);
        }
    }

    @Override
    public void bind() {
        instance.bind();
    }

    @Override
    public void unbind() {
        instance.unbind();
    }

    @Override
    public void cleanup() {
        instance.cleanup();
    }

    @Override
    public void link() throws ShaderException {
        instance.link();
    }

    @Override
    public void setPointLight(int lightNumber, Vector3f mPosition, Color4f color) {
        instance.setPointLight(lightNumber, mPosition, color);
    }

    @Override
    public void setProjectionMatrix(Matrix4f viewProjectionMatrix) {
        instance.setProjectionMatrix(viewProjectionMatrix);
    }

    @Override
    public void setModelMatrix(Matrix4f modelMatrix) {
        instance.setModelMatrix(modelMatrix);
    }

    @Override
    public void setNormalMatrix(Matrix3f normalMatrix) {
        instance.setNormalMatrix(normalMatrix);
    }

    @Override
    public void setMaterial(Color4f diffuse, Color4f specular, float reflectance) {
        instance.setMaterial(diffuse, specular, reflectance);
    }
}
