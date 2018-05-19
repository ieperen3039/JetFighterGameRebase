package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Rendering.Shaders.AbstractShader;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderException;
import nl.NG.Jetfightergame.Tools.Resources;
import nl.NG.Jetfightergame.Tools.Toolbox;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;

import static nl.NG.Jetfightergame.Tools.Directory.shaders;
import static org.lwjgl.opengl.GL20.*;

/**
 * @author Geert van Ieperen created on 17-5-2018.
 */
public class ParticleShader {

    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    private final int timeUniform;
    private final int projectionUniform;

    public ParticleShader() throws IOException {
        programId = glCreateProgram();

        final String vertexCode = Resources.loadText(shaders.pathOf("Particle/vertex.vert"));
        vertexShaderId = AbstractShader.createShader(programId, GL_VERTEX_SHADER, vertexCode);

        final String fragmentCode = Resources.loadText(shaders.pathOf("Gouraud/fragment.frag"));
        fragmentShaderId = AbstractShader.createShader(programId, GL_FRAGMENT_SHADER, fragmentCode);

        link();

        timeUniform = glGetUniformLocation(programId, "time");
        projectionUniform = glGetUniformLocation(programId, "viewProjectionMatrix");
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    private void link() throws ShaderException {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);


        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            Toolbox.printError("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    /**
     * Set the time uniform of this program
     * @param value The new value of the uniform.
     */
    public void setTime(float value) {
        glUniform1f(timeUniform, value);
    }

    public void setProjection(Matrix4fc matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(projectionUniform, false, fb);
        }
    }
}
