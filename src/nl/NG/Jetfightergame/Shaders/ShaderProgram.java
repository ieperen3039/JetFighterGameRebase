package nl.NG.Jetfightergame.Shaders;

import nl.NG.Jetfightergame.Shaders.shader.DirectionalLight;
import nl.NG.Jetfightergame.Shaders.shader.PointLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 *  @author Yoeri Poels
 */
public class ShaderProgram {

    public final static int MAX_POINT_LIGHTS = 5;

    private final Map<String, Integer> uniforms;

    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private float specularPower = 1f;
    private Vector3f ambientLight = new Vector3f();
    private DirectionalLight directionalLight = new DirectionalLight(
            new Vector3f(),
            new Vector3f(),
            1f
    );
    private PointLight[] pointLights = {};

    public ShaderProgram() throws ShaderException {
        uniforms = new HashMap<>();

        programId = glCreateProgram();
        if (programId == 0) {
            throw new ShaderException("Could not create Shader");
        }
    }

    /**
     * Bind the renderer to the current rendering state
     */
    public void bind() {
        glUseProgram(programId);
    }

    /**
     * Unbind the renderer from the current rendering state
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * Cleanup the renderer after it's done
     */
    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    /**
     * Link the program and cleanup the shaders.
     *
     * @throws ShaderException If an error occures linking the shader code.
     */
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
     * Get the ambient light vector.
     *
     * @return The ambient light vector.
     */
    public Vector3f getAmbientLight() {
        return this.ambientLight;
    }

    /**
     * Set the ambient light vector.
     *
     * @param ambientLight the ambient light vector.
     */
    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    /**
     * Get the specular power.
     *
     * @return the specular power.
     */
    public float getSpecularPower() {
        return specularPower;
    }

    /**
     * Set the specular power.
     *
     * @param specularPower the specular power.
     */
    void setSpecularPower(float specularPower) {
        this.specularPower = specularPower;
    }

    /**
     * Get the directional light.
     *
     * @return the DirectionalLight
     */
    public DirectionalLight getDirectionalLight() {
        return this.directionalLight;
    }

    /**
     * Set the directional light.
     *
     * @param directionalLight the DirectionalLight
     */
    public void setDirectionalLight(DirectionalLight directionalLight) {
        this.directionalLight = directionalLight;
    }

    /**
     * Get a list of pointlights.
     *
     * @return the list of pointlights.
     */
    public PointLight[] getPointLights() {
        return this.pointLights;
    }

    /**
     * Set a pointlight.
     *
     * @param pointLight The pointlight to set.
     * @param i The index in the pointlight array.
     */
    public void setPointLight(PointLight pointLight, int i) {
        this.pointLights[i] = pointLight;
    }

    /**
     * Create an uniform for a pointslight array.
     *
     * @param uniformName The name of the uniform.
     * @param size The size of the array.
     * @throws ShaderException If an error occurs getting the memory location.
     */
    public void createPointLightsUniform(String uniformName, int size) throws ShaderException {
        for (int i = 0; i < size; i++) {
            createPointLightUniform(uniformName + "[" + i + "]");
        }
    }

    /**
     * Create the uniforms required for a PointLight
     *
     * @param uniformName The name of the uniform
     * @throws ShaderException If an error occurs getting the memory location.
     */
    public void createPointLightUniform(String uniformName) throws ShaderException {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    /**
     * Create the uniforms required for a DirectionalLight
     *
     * @param uniformName The name of the uniform
     * @throws ShaderException If an error occurs getting the memory location.
     */
    public void createDirectionalLightUniform(String uniformName) throws ShaderException {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }

    /**
     * Create the uniforms required for a Material
     *
     * @param uniformName The name of the uniform
     * @throws ShaderException If an error occurs getting the memory location.
     */
    public void createMaterialUniform(String uniformName) throws ShaderException {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".hasTexture");
        createUniform(uniformName + ".reflectance");
    }

    /**
     * Create a new uniform and get its memory location.
     *
     * @param uniformName The name of the uniform.
     * @throws ShaderException If an error occurs getting the memory location.
     */
    public void createUniform(String uniformName) throws ShaderException {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new ShaderException("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Set the value of a certain 4x4 matrix shader uniform.
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    /**
     * Set the value of a certain integer shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    /**
     * Set the value of a certain float shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    /**
     * Set the value of a certain 3D Vector shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    public void setUniform4f(String uniformName, float[] value){
        glUniform4f(uniforms.get(uniformName), value[0], value[1], value[2], value[3]);
    }

    /**
     * Set the value of a certain 4D Vector shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param value The new value of the uniform.
     */
    public void setUniform4f(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, PointLight pointlight, int i) {
        setUniform(uniformName + "[" + i + "]", pointlight);
    }

    /**
     * Set the value of a certain PointLight shader uniform
     *
     * @param uniformName The name of the uniform.
     * @param pointLight The new value of the uniform.
     */
    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".colour", pointLight.getColor() );
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }

    /**
     * Create a new vertexshader and set the vertexshader id field to that of the newly created shader.
     *
     * @param shaderCode The shaderCode as a String.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    public void createVertexShader(String shaderCode) throws ShaderException {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    /**
     * Create a new fragmentshader and set the fragmentshader id field to that of the newly created shader.
     *
     * @param shaderCode The shaderCode as a String.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    public void createFragmentShader(String shaderCode) throws ShaderException {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    /**
     * Create a new shader and return the id of the newly created shader.
     *
     * @param shaderCode The shaderCode as a String.
     * @param shaderType The type of shader, e.g. GL_VERTEX_SHADER.
     * @return The id of the newly created shader.
     * @throws ShaderException If an error occurs during the creation of a shader.
     */
    public int createShader(String shaderCode, int shaderType) throws ShaderException {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new ShaderException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new ShaderException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }
}
