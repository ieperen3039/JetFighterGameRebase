package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameState.Environment;
import nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShaderUniformGL;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderException;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private Environment gameState;

    private final String sessionName;
    private int frameNumber = 0;

    public JetFighterRenderer(JetFighterGame engine, EnvironmentManager gameState, GLFWWindow window,
                              Camera camera, ControllerManager controllerManager, AbstractJet player) throws IOException, ShaderException {
        super(
                "Rendering loop", ClientSettings.TARGET_FPS, false,
                (ex) -> {
                    window.close();
                    engine.exitGame();
                }
        );

        this.gameState = gameState;
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;

        shaderManager = new ShaderManager();

        final Runnable cameraMode = () -> {
            if (ServerSettings.SPECTATOR_MODE) {
                engine.setSpectatorMode();
            } else {
                engine.setPlayMode();
            }
        };

        new JetFighterMenu(window::getWidth, window::getHeight, cameraMode, engine::exitGame, controllerManager, shaderManager, gameState);
        new GravityHud(window::getWidth, window::getHeight, player, camera).activate();

        SimpleDateFormat ft = new SimpleDateFormat("yymmdd_hhmmss");
        sessionName = ft.format(new Date());
    }

    @Override
    protected void update(float realDeltaTime) {
        GameTimer timer = gameState.getTimer();
        timer.updateRenderTime();
        activeCamera.updatePosition(timer.getRenderTime().difference());
        frameNumber++;

        Toolbox.checkGLError();

        Color4f ambientLight = gameState.fogColor();
        float fog = Math.min(ClientSettings.Z_FAR, (1f /ambientLight.alpha)); // also considers div/0
        ambientLight = new Color4f(ambientLight, 1f);
        window.setClearColor(ambientLight);
        shaderManager.initShader(activeCamera, ambientLight, fog);
        Toolbox.checkGLError();

        ShaderUniformGL gl = new ShaderUniformGL(shaderManager, window.getWidth(), window.getHeight(), activeCamera);
        Toolbox.checkGLError();

        if (ClientSettings.CULL_FACES) {
            // Cull backfaces
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }

        // scene lighting
        gl.setLight(activeCamera.getEye(), Color4f.GREY);
        gameState.setLights(gl);
        Toolbox.checkGLError();

        // first draw the non-transparent objects
        gameState.drawObjects(gl);
        Toolbox.checkGLError();

        // overlay with transparent objects
        // TODO transparent meshes?

        if (ClientSettings.HIGHLIGHT_LINE_WIDTH > 0){
            gl.setFill(false);
            gameState.drawObjects(gl);
            Toolbox.checkGLError();
            gl.setFill(true);
        }

        // particles
        glDisable(GL_CULL_FACE);
        gameState.drawParticles(gl);
        Toolbox.checkGLError();

        shaderManager.unbind();

        // HUD / menu
        ScreenOverlay.draw(window.getWidth(), window.getHeight(), gl::getPositionOnScreen, activeCamera.getEye());

        // update window
        window.update();

        if (ClientSettings.SAVE_PLAYBACK && !engine.isPaused()) {
            window.printScreen("session_" + sessionName + "/" + frameNumber);
        }

        // update stop-condition
        if (window.shouldClose()) {
            engine.exitGame();
        }
        Toolbox.checkGLError();

    }

    @Override
    public void cleanup() {
        shaderManager.cleanup();
    }
}
