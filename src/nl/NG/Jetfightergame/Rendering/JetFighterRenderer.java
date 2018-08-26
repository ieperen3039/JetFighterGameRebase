package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShaderUniformGL;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleShader;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderException;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Engine.JetFighterGame.GameMode.MENU_MODE;
import static nl.NG.Jetfightergame.Settings.ClientSettings.TARGET_FPS;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 */
public class JetFighterRenderer extends AbstractGameLoop {
    private final Consumer<ScreenOverlay.Painter> HUD;
    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;
    private ShaderManager shaderManager;
    private Environment gameState;
    private ScreenOverlay overlay;
    private ParticleShader particleShader;
    private final JetFighterMenu menu;

    private final String sessionName;
    private long frameNumber = 0;
    private Mode displayMode;
    private boolean hudIsDisabled = false;

    public enum Mode {
        SHOW, RECORD_AND_SHOW, RECORD
    }

    public JetFighterRenderer(JetFighterGame engine, Environment gameState, GLFWWindow window, Camera camera,
                              ControllerManager controllerManager, Consumer<ScreenOverlay.Painter> hudProvider, Mode displayMode
    ) throws IOException, ShaderException {
        super("Rendering", TARGET_FPS, false);

        this.gameState = gameState;
        this.window = window;
        this.activeCamera = camera;
        this.engine = engine;
        this.displayMode = displayMode;

        Logger.printOnline(() -> {
            Float currentTime = engine.getTimer().getRenderTime().current();
            return "Particles: " + gameState.getParticleCount(currentTime);
        });

        shaderManager = new ShaderManager();
        particleShader = new ParticleShader();

        overlay = new ScreenOverlay(() -> engine.getCurrentGameMode() == MENU_MODE);
        overlay.addHudItem((hud) -> {
            if (ClientSettings.DEBUG_SCREEN) {
                Logger.setOnlineOutput(hud::printRoll);
            }
        });

        menu = new JetFighterMenu(
                window::getWidth, window::getHeight,
                engine::setPlayMode, engine::exitGame,
                controllerManager, shaderManager, overlay::isMenuMode
        );
        HUD = hudProvider;

        overlay.addMenuItem(menu);
        overlay.addHudItem(HUD);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_hhmmss");
        sessionName = dateFormat.format(new Date());
    }

    @Override
    protected void update(float realDeltaTime) {
        GameTimer timer = engine.getTimer();
        timer.updateRenderTime();
        timer.updateGameTime(); // will never run together with serverloop

        Float currentRenderTime = timer.getRenderTime().current();
        Float deltaRenderTime = timer.getRenderTime().difference();

        activeCamera.updatePosition(deltaRenderTime);
        frameNumber++;

        // shader preparation and background
        Color4f ambientLight = gameState.fogColor();
        float fogRange = Math.min(ClientSettings.Z_FAR, (1f / ambientLight.alpha)); // also considers div/0
        ambientLight = new Color4f(ambientLight, 1f);
        window.setClearColor(ambientLight);
        shaderManager.initShader(activeCamera, ambientLight, fogRange);

        ShaderUniformGL gl = new ShaderUniformGL(shaderManager, window.getWidth(), window.getHeight(), activeCamera);

        if (ClientSettings.CULL_FACES) {
            // Cull backfaces
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }

        // scene lighting
        gl.setLight(activeCamera.getEye(), Color4f.GREY);
        gameState.setLights(gl);

        // first draw the non-transparent objects
        gameState.drawObjects(gl);

        // overlay with transparent objects
        // TODO transparent meshes?

        // line highlighting
        if (ClientSettings.HIGHLIGHT_LINE_WIDTH > 0) {
            gl.setFill(false);
            gameState.drawObjects(gl);
            gl.setFill(true);
        }

        shaderManager.unbind();

        // particles
        glDisable(GL_CULL_FACE);
        particleShader.bind();
        particleShader.setTime(currentRenderTime);
        particleShader.setProjection(gl.getProjection());

        gameState.drawParticles(currentRenderTime);
        particleShader.unbind();

        // HUD / menu
        overlay.draw(window.getWidth(), window.getHeight(), gl::getPositionOnScreen, activeCamera.getEye());

        // update window
        if (displayMode == Mode.RECORD_AND_SHOW || displayMode == Mode.SHOW) {
            window.update();
        }

        if (displayMode == Mode.RECORD_AND_SHOW || displayMode == Mode.RECORD) {
            if (!engine.isPaused()) {
                boolean canUseFront = (displayMode == Mode.RECORD_AND_SHOW);
                Path path = Paths.get("session_" + sessionName, String.valueOf(frameNumber));
                window.printScreen(Directory.recordings, canUseFront, path.toString());
            }
        }

        // update stop-condition
        if (window.shouldClose()) {
            engine.exitGame();
        }

        Toolbox.checkGLError();
    }

    public JetFighterMenu getMainMenu() {
        return menu;
    }

    public void toggleHud() {
        if (hudIsDisabled) {
            overlay.addHudItem(HUD);
            hudIsDisabled = false;
        } else {
            overlay.removeHudItem(HUD);
            hudIsDisabled = true;
        }
    }

    @Override
    protected void exceptionHandler(Exception ex) {
        super.exceptionHandler(ex);
        engine.exitGame();
    }

    @Override
    public void cleanup() {
        shaderManager.cleanup();
        overlay.removeHudItem(HUD);
        overlay.removeMenuItem(menu);
    }
}
