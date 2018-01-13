package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShaderUniformGL;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Engine.Managers.ControllerManager;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderException;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.Scenarios.Environment;
import nl.NG.Jetfightergame.Scenarios.EnvironmentManager;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Sound.MusicProvider;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 */
public class JetFighterRenderer extends AbstractGameLoop {

    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;

    private ShaderManager shaderManager;

    private Color4f ambientLight;
    private Environment gameState;

    public JetFighterRenderer(JetFighterGame engine, EnvironmentManager gameState, GLFWWindow window,
                              Camera camera, MusicProvider musicProvider, ControllerManager controllerManager) throws IOException, ShaderException {
        super("Rendering loop", Settings.TARGET_FPS, false, (ex) -> engine.exitGame());

        this.gameState = gameState;
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        // TODO allow toggle shader
        shaderManager = new ShaderManager();

        ambientLight = Color4f.LIGHT_GREY;
        window.setClearColor(ambientLight);

        new JetFighterMenu(window::getWidth, window::getHeight, musicProvider, engine::setSpectatorMode, engine::exitGame, controllerManager, shaderManager, gameState);
        new GravityHud(window::getWidth, window::getHeight, engine.getPlayer(), camera);
    }

    @Override
    protected void update(float realDeltaTime) {
        try {
            GameTimer timer = gameState.getTimer();
            timer.updateRenderTime();
            activeCamera.updatePosition(timer.getRenderTime().difference());

            Toolbox.checkGLError();
            GL2 gl = new ShaderUniformGL(shaderManager, window.getWidth(), window.getHeight(), activeCamera);
            Toolbox.checkGLError();


            if (Settings.CULL_FACES) {
                // Cull backfaces
                glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
            }

            shaderManager.initShader(activeCamera, ambientLight);
            Toolbox.checkGLError();

            if (!engine.isPaused()) gameState.updateParticles();

            // scene lighting
            activeCamera.cameraLighting(gl);
            gameState.setLights(gl);
            Toolbox.checkGLError();

            // first draw the non-transparent objects
            gameState.drawObjects(gl);
            Toolbox.checkGLError();
            gameState.drawParticles(gl);
            Toolbox.checkGLError();

            // overlay with transparent objects
            // TODO transparent meshes?

            shaderManager.unbind();

            ScreenOverlay.draw(window.getWidth(), window.getHeight());

            // update window
            window.update();

            // update stop-condition
            if (window.shouldClose()) {
                engine.exitGame();
            }
            Toolbox.checkGLError();

        } catch (Exception ex){ // this catch clause seems to be redundant
            window.close();
            engine.exitGame();
            throw ex;
        }
    }

    @Override
    public void cleanup() {
        shaderManager.cleanup();
    }
}
