package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.Shaders.GouraudShader;
import nl.NG.Jetfightergame.Shaders.PhongShader;
import nl.NG.Jetfightergame.Shaders.ShaderException;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Vectors.Color4f;

import java.io.IOException;

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

    private Color4f ambientLight;

    public JetFighterRenderer(GLFWWindow window, Camera camera, JetFighterGame engine) throws IOException, ShaderException {
        super("Rendering loop", Settings.TARGET_FPS, false);
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        gouraudShader = new GouraudShader();
        phongShader = new PhongShader(); //TODO allow toggle
        currentShader = gouraudShader;
        window.setClearColor(0.8f, 0.8f, 0.8f, 1.0f);

//        // use built-in Gouraud shading
//        glShadeModel( GL_FLAT );

        ambientLight = new Color4f(0.5f, 0.5f, 0.5f);
        this.hud = new Hud(window);
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


        if (currentShader instanceof PhongShader){
            PhongShader shader = (PhongShader) currentShader;
            shader.setSpecular(1f);
            shader.setBlack(true);
            shader.setShadowed(true);
            shader.setAmbientLight(ambientLight);
        } else if (currentShader instanceof GouraudShader){
            GouraudShader shader = (GouraudShader) currentShader;
            shader.setAmbientLight(ambientLight);
        }

        GL2 gl = new ShaderUniformGL(currentShader, windowWidth, windowHeight, activeCamera);
        currentShader.bind();


        if (!engine.isPaused()) engine.updateParticles(deltaTime);


        // first draw the non-transparent objects
        engine.drawObjects(gl);
        engine.drawParticles(gl);
        // overlay with transparent objects

        currentShader.unbind();

        hud.draw(windowWidth, windowHeight);

        // update window
        window.update();
        // update stop-condition
        if (window.shouldClose()) {
            stopLoop();
            engine.exitGame();
        }

    }

}
