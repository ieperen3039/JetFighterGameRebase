package nl.NG.Jetfightergame.Shaders.shader;

import nl.NG.Jetfightergame.Shaders.ShaderException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Yoeri Poels.
 */
public class DepthMap {


    public int width;
    public int height;

    private int depthFboID;

    private Texture depthMap;

    public DepthMap(int size) {
        width = size;
        height = size;
        // Create an FBO to render the depth map
        depthFboID = glGenFramebuffers();

        // Create the depth map texture
        depthMap = new Texture(width, height, GL_DEPTH_COMPONENT);

        // Attach the the depth map texture to the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthFboID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap.getId(), 0);
        // Only draw the depth, no colours
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new ShaderException("Failed creating frame buffer.");
        }

        // Unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Texture getDepthMapTexture() {
        return depthMap;
    }

    public int getDepthMapFBO() {
        return depthFboID;
    }

    public void cleanup() {
        glDeleteFramebuffers(depthFboID);
        depthMap.cleanup();
    }
}
