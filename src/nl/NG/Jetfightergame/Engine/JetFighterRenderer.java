package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.Engine.Window.GLFWWindow;
import nl.NG.Jetfightergame.Shaders.ShaderException;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Tools.Resource;

import java.awt.*;
import java.io.IOException;

import static nl.NG.Jetfightergame.Shaders.ShaderProgram.MAX_POINT_LIGHTS;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Jorren Hendriks.
 */
class JetFighterRenderer extends GameLoop {

    private GLFWWindow window;
    private Camera activeCamera;

    // generic shader
    private final ShaderProgram gouraudShader;
    // advanced shader
    private final ShaderProgram phongShader;

    private ShaderProgram currentShader;

    private Color ambientLight;

    private boolean shadowMapping = false;

    public JetFighterRenderer(GLFWWindow window, Camera camera) throws IOException, ShaderException {
        super(Settings.TARGET_TPS);
        this.window = window;
        this.activeCamera = camera;

        gouraudShader = initGouraudShader();
        phongShader = initPhongShader(); //TODO allow toggle
        currentShader = gouraudShader;

//        // use built-in Gouraud shading
//        glShadeModel( GL_FLAT );

        ambientLight = Settings.AMBIENT_LIGHT;

    }

    private ShaderProgram initGouraudShader() throws ShaderException, IOException {
        ShaderProgram gouraudShader = new ShaderProgram();
        gouraudShader.createVertexShader(Resource.load("/shaders/vertex_depth.vert"));
        gouraudShader.createFragmentShader(Resource.load("/shaders/fragment_depth.frag"));
        gouraudShader.link();

        // Create uniforms for world and projection matrices
        gouraudShader.createUniform("projectionMatrix");
        gouraudShader.createUniform("modelViewMatrix");

        // Create the Material uniform
        gouraudShader.createMaterialUniform("material");
        gouraudShader.createUniform("transparency");
        // Create the lighting uniforms
        gouraudShader.createPointLightsUniform("pointLights", MAX_POINT_LIGHTS);
        return gouraudShader;
    }

    private ShaderProgram initPhongShader() throws ShaderException, IOException {
        ShaderProgram phongShader = new ShaderProgram();
        phongShader.createVertexShader(Resource.load("/shaders/vertex.vert"));
        phongShader.createFragmentShader(Resource.load("/shaders/fragment.frag"));
        phongShader.link();

        // Create uniforms for world and projection matrices
        phongShader.createUniform("projectionMatrix");
        phongShader.createUniform("modelViewMatrix");
        // Create the Material uniform
        phongShader.createMaterialUniform("material");
        // Create the lighting uniforms
        phongShader.createUniform("specularPower");
        phongShader.createUniform("ambientLight");
        phongShader.createPointLightsUniform("pointLights", MAX_POINT_LIGHTS);

        // Create uniform for special lighting conditions for background elements
        phongShader.createUniform("blackAsAlpha");
        phongShader.createUniform("shadowed");

        return phongShader;
    }

    public void cleanup() {
        phongShader.cleanup();
        gouraudShader.cleanup();
    }

    @Override
    protected void update(float deltaTime) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        currentShader.bind();

        if (currentShader == phongShader){
            // TODO set uniforms
            /*
            uniform float specularPower;
            uniform Material material;
            uniform PointLight pointLights[MAX_POINT_LIGHTS];
            uniform vec3 camera_pos;
            uniform bool shadowed;
            uniform bool blackAsAlpha;
            */
        }

        currentShader.createPointLightUniform("pointlight");
        currentShader.createMaterialUniform("material");

        GL2 gl = new ShaderUniformGL(currentShader);

        //first draw non transparent meshes

        //overlay with transparent meshes

        currentShader.unbind();
    }

    @Override
    protected boolean shouldStop() {
        return false;
    }
}
