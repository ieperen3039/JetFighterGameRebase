package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.Engine.Window.GLFWWindow;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Shaders.ShaderException;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Tools.Resource;
import org.joml.Vector3f;

import java.io.IOException;

import static nl.NG.Jetfightergame.Engine.Settings.MAX_POINT_LIGHTS;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Jorren Hendriks.
 */
public class JetFighterRenderer extends AbstractGameLoop {

    private final Hud hud;
    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;

    // generic shader
    private final ShaderProgram gouraudShader;
    // advanced shader
    private final ShaderProgram phongShader;

    private ShaderProgram currentShader;

    private Vector3f ambientLight;

    public JetFighterRenderer(GLFWWindow window, Camera camera, JetFighterGame engine) throws IOException, ShaderException {
        super(Settings.TARGET_TPS);
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        gouraudShader = initGouraudShader();
        phongShader = initPhongShader(); //TODO allow toggle
        currentShader = gouraudShader;

//        // use built-in Gouraud shading
//        glShadeModel( GL_FLAT );

        ambientLight = Settings.AMBIENT_LIGHT;
        this.hud = new Hud(window);
    }

    private ShaderProgram initGouraudShader() throws ShaderException, IOException {
        ShaderProgram gouraudShader = new ShaderProgram();
        gouraudShader.createVertexShader(Resource.load("res/shaders/Gouraud/vertex.vert"));
        gouraudShader.createFragmentShader(Resource.load("res/shaders/Gouraud/fragment.frag"));
        gouraudShader.link();

        // Create uniforms for world and projection matrices
        gouraudShader.createUniform("projectionMatrix");
        gouraudShader.createUniform("modelViewMatrix");

        // Create the Material uniform
        gouraudShader.createMaterialUniform("material");
        // Create the lighting uniforms
        gouraudShader.createUniform("ambientLight");
        gouraudShader.createPointLightsUniform("pointLights", MAX_POINT_LIGHTS);
        return gouraudShader;
    }

    private ShaderProgram initPhongShader() throws ShaderException, IOException {
        ShaderProgram phongShader = new ShaderProgram();
        phongShader.createVertexShader(Resource.load("res/shaders/Phong/vertex.vert"));
        phongShader.createFragmentShader(Resource.load("res/shaders/Phong/fragment.frag"));
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

    @Override
    public void cleanup() {
        phongShader.cleanup();
        gouraudShader.cleanup();
    }

    @Override
    protected void update(float deltaTime) throws InterruptedException {

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, windowWidth, windowHeight);

        currentShader.bind();

        currentShader.setUniform("ambientLight", Settings.AMBIENT_LIGHT);

        if (currentShader == phongShader) {
            currentShader.setUniform("specularPower", 1f);
            currentShader.setUniform("cameraPos", activeCamera.getEye().toVector3f());
            currentShader.setUniform("blackAsAlpha", true);
            currentShader.setUniform("shadowed", true);
        }

        if (!engine.isPaused()) engine.updateParticles(deltaTime);

        GL2 gl = new ShaderUniformGL(currentShader);
        setView(gl, windowWidth, windowHeight);

        //first drawObjects non transparent meshes
        engine.drawObjects(gl);
        engine.drawParticles(gl);
        //overlay with transparent meshes

        currentShader.unbind();

        hud.draw(windowWidth, windowHeight);

        // update window
        window.update();
    }

    @Override
    protected boolean shouldStop() {
        return window.shouldClose();
    }

    public void setView(GL2 gl, int windowWidth, int windowHeight) {

        // Set the perspective.
        gl.setFustrum(windowWidth , windowHeight);

        // Update the view
        gl.setCamera(activeCamera);
    }

    @Override
    protected String getName() {
        return "Rendering";
    }
}
