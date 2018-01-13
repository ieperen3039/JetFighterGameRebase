package nl.NG.Jetfightergame.Rendering.Shaders;

import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 *  @author Yoeri Poels
 *  @author Geert van Ieperen
 */
public abstract class AbstractShader implements ShaderProgram {

    private final Map<String, Integer> uniforms;

    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public AbstractShader(String vertexPath, String fragmentPath) throws ShaderException, IOException {
        uniforms = new HashMap<>();

        programId = glCreateProgram();
        if (programId == 0) {
            throw new ShaderException("Could not create Shader");
        }

        if (vertexPath != null)
            vertexShaderId = createShader(Resource.load(vertexPath), GL_VERTEX_SHADER);

        if (fragmentPath != null)
            fragmentShaderId = createShader(Resource.load(fragmentPath), GL_FRAGMENT_SHADER);

        link();

        // Create uniforms for world and projection matrices
        createUniform("viewProjectionMatrix");
        createUniform("modelMatrix");
        createUniform("normalMatrix");
    }

    @Override
    public void bind() {
        glUseProgram(programId);
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    @Override
    public void link() throws ShaderException {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

    }

    /**
     * Create a new uniform and get its memory location.
     *
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs while fetching the memory location.
     */
    protected void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Set the value of a 4x4 matrix shader uniform.
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a 3x3 matrix shader uniform.
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform(String uniformName, Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(9);
            value.get(fb);
            glUniformMatrix3fv(uniforms.get(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a certain integer shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    /**
     * Set the value of a certain float shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    /**
     * Set the value of a certain 3D Vector shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    protected void setUniform4f(String uniformName, float[] value){
        glUniform4f(uniforms.get(uniformName), value[0], value[1], value[2], value[3]);
    }

    /**
     * Set the value of a certain 4D Vector shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    protected void setUniform4f(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    /**
     * Set the value of a certain PointLight shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param mPosition position in modelSpace
     * @param color the light color with its intensity as alpha value
     */
    protected void setPointLightUniform(String uniformName, Vector3f mPosition, Color4f color) {
        setUniform(uniformName + ".color", color.toVector3f());
        setUniform(uniformName + ".mPosition", mPosition);
        setUniform(uniformName + ".intensity", color.alpha);
    }

    protected void setUniform(String uniformName, Color4f color) {
        setUniform(uniformName, color.toVector3f().div(color.alpha));
        Toolbox.checkGLError();
    }

    /**
     * Create a new shader and return the id of the newly created shader.
     *
     * @param shaderCode The shaderCode as a String.
     * @param shaderType The type of shader, e.g. GL_VERTEX_SHADER.
     * @return The id of the newly created shader.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    private int createShader(String shaderCode, int shaderType) throws ShaderException {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new ShaderException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new ShaderException("Error compiling Shader code:\n" + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    protected void setUniform(String uniformName, boolean value) {
        setUniform(uniformName, value ? 1 : 0);
    }

    @Override
    public void setProjectionMatrix(Matrix4f viewProjectionMatrix) {
        setUniform("viewProjectionMatrix", viewProjectionMatrix);
    }

    @Override
    public void setModelMatrix(Matrix4f modelMatrix) {
        setUniform("modelMatrix", modelMatrix);
    }

    @Override
    public void setNormalMatrix(Matrix3f normalMatrix){
        setUniform("normalMatrix", normalMatrix);
    }

    @Override
    public void setMaterial(Material mat){
        setMaterial(mat, Color4f.WHITE);
    }

}