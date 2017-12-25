package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLFWWindow;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Engine.Managers.ControllerManager;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Shaders.GouraudShader;
import nl.NG.Jetfightergame.Shaders.PhongShader;
import nl.NG.Jetfightergame.Shaders.ShaderException;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Sound.MusicProvider;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;

import java.io.IOException;
import java.util.function.BooleanSupplier;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 */
public class JetFighterRenderer extends AbstractGameLoop {

    private final ScreenOverlay screenOverlay;
    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;

    private ShaderProgram currentShader;

    private Color4f ambientLight;
    private GameState gameState;

    public JetFighterRenderer(JetFighterGame engine, GameState gameState, GLFWWindow window,
                              Camera camera, MusicProvider musicProvider, ControllerManager input, BooleanSupplier menuMode) throws IOException, ShaderException {
        super("Rendering loop", Settings.TARGET_FPS, false);

        this.gameState = gameState;
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        // TODO allow toggle shader
        currentShader = new GouraudShader();

        window.setClearColor(0.8f, 0.8f, 0.8f, 1.0f);

        ambientLight = Color4f.LIGHT_GREY;
        this.screenOverlay = new ScreenOverlay(menuMode);

        new JetFighterMenu(screenOverlay, musicProvider, engine::setPlayMode, engine::exitGame, input);
        new GravityHud(screenOverlay, window.getDimensions(), engine.getPlayer(), camera);
    }

    @Override
    protected void update(float realDeltaTime) {
        try {
            Toolbox.checkGLError();
            GL2 gl = new ShaderUniformGL(currentShader, window.getWidth(), window.getHeight(), activeCamera);
            Toolbox.checkGLError();

            gameState.updateRenderTime();

            // update camera based on
            activeCamera.updatePosition(gameState.time.getRenderTime().difference());

            initShader();
            Toolbox.checkGLError();

            if (!engine.isPaused()) gameState.updateParticles();

            // activate lights in the scene
            gameState.setLights(gl);
            Toolbox.checkGLError();

            // first draw the non-transparent objects
            gameState.drawObjects(gl);
            Toolbox.checkGLError();
            gameState.drawParticles(gl);
            Toolbox.checkGLError();

            // overlay with transparent objects
            // TODO transparent meshes?

            currentShader.unbind();

            screenOverlay.draw(window.getWidth(), window.getHeight());

            // update window
            window.update();

            // update stop-condition
            if (window.shouldClose()) {
                engine.exitGame();
            }
            Toolbox.checkGLError();

        } catch (Exception ex){
            window.close();
            engine.exitGame();
            throw ex;
        }
    }

    private void initShader() {
        currentShader.bind();

        if (currentShader instanceof PhongShader){
            PhongShader shader = (PhongShader) currentShader;
            shader.setSpecular(1f);
            shader.setBlack(false);
            shader.setShadowed(true);
            shader.setAmbientLight(ambientLight);
            shader.setCameraPosition(activeCamera.getEye());
        } else if (currentShader instanceof GouraudShader){
            GouraudShader shader = (GouraudShader) currentShader;
            shader.setAmbientLight(ambientLight);
            shader.setCameraPosition(activeCamera.getEye());
        } else {
            Toolbox.print("loaded shader without advanced parameters: " + currentShader.getClass().getSimpleName());
        }
    }

    @Override
    public void cleanup() {
        currentShader.cleanup();
    }
}
