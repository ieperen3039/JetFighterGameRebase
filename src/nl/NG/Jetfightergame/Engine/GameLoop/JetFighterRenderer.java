package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLFWWindow;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;
import nl.NG.Jetfightergame.ScreenOverlay.HudMenu;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.Shaders.GouraudShader;
import nl.NG.Jetfightergame.Shaders.PhongShader;
import nl.NG.Jetfightergame.Shaders.ShaderException;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Sound.MusicProvider;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;

import java.io.IOException;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 */
public class JetFighterRenderer extends AbstractGameLoop {

    private final Hud hud;
    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;
    private final HudMenu gameMenu;

    private ShaderProgram currentShader;

    private Color4f ambientLight;

    public JetFighterRenderer(GLFWWindow window, Camera camera, JetFighterGame engine, MusicProvider musicProvider, boolean inMenuMode) throws IOException, ShaderException {
        super("Rendering loop", Settings.TARGET_FPS, false);
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        // TODO allow toggle shader
        currentShader = new GouraudShader();

        window.setClearColor(0.8f, 0.8f, 0.8f, 1.0f);

        ambientLight = Color4f.LIGHT_GREY;
        this.hud = new Hud(window);

        gameMenu = new JetFighterMenu(hud, musicProvider, engine::setPlayMode, engine::exitGame, inMenuMode);
    }

    @Override
    protected void update(float deltaTime) {
        Toolbox.checkGLError();
        GL2 gl = new ShaderUniformGL(currentShader, window.getWidth(), window.getHeight(), activeCamera);
        Toolbox.checkGLError();

        initShader();
        Toolbox.checkGLError();

        engine.updateParticles(deltaTime);

        // activate lights in the scene
        engine.setLights(gl);
        Toolbox.checkGLError();

        // first draw the non-transparent objects
        engine.drawObjects(gl);
        Toolbox.checkGLError();
        engine.drawParticles(gl);
        Toolbox.checkGLError();

        // overlay with transparent objects
        // TODO transparent meshes?

        currentShader.unbind();

        hud.draw(window.getWidth(), window.getHeight());

        // update window
        window.update();

        // update stop-condition
        if (window.shouldClose()) {
            engine.exitGame();
        }
        Toolbox.checkGLError();
    }

    private void initShader() {
        currentShader.bind();

        if (currentShader instanceof PhongShader){
            PhongShader shader = (PhongShader) currentShader;
            shader.setSpecular(1f);
            shader.setBlack(false);
            shader.setShadowed(true);
            shader.setAmbientLight(ambientLight);
            shader.setCameraPosition(activeCamera.getEye().toVector3f());
        } else if (currentShader instanceof GouraudShader){
            GouraudShader shader = (GouraudShader) currentShader;
            shader.setAmbientLight(ambientLight);
            shader.setCameraPosition(activeCamera.getEye().toVector3f());
        } else {
            Toolbox.print("loaded shader without advanced parameters: " + currentShader.getClass().getSimpleName());
        }
    }

    @Override
    public void cleanup() {
        currentShader.cleanup();
    }
}
