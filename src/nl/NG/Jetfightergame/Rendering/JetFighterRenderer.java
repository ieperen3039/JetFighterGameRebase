package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShaderUniformGL;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderException;
import nl.NG.Jetfightergame.Rendering.Shaders.ShaderManager;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.GravityHud;
import nl.NG.Jetfightergame.ScreenOverlay.JetFighterMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.NG.Jetfightergame.Engine.GLFWGameEngine.GameMode.MENU_MODE;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 */
public class JetFighterRenderer extends AbstractGameLoop {

    private final GravityHud gravityHud;
    private GLFWWindow window;
    private Camera activeCamera;
    private final JetFighterGame engine;
    private ShaderManager shaderManager;
    private Environment gameState;
    private ScreenOverlay overlay;

    private final String sessionName;
    private int frameNumber = 0;

    public JetFighterRenderer(JetFighterGame engine, Environment gameState, GLFWWindow window,
                              Camera camera, ControllerManager controllerManager, AbstractJet player) throws IOException, ShaderException {
        super(
                "Rendering", ClientSettings.TARGET_FPS, false,
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

        overlay = new ScreenOverlay(() -> engine.getCurrentGameMode() == MENU_MODE);
        overlay.addHudItem((hud) -> Toolbox.printOnline(hud::printRoll));

        new JetFighterMenu(
                window::getWidth, window::getHeight,
                engine::setPlayMode, engine::exitGame,
                controllerManager, shaderManager, overlay
        );

        gravityHud = new GravityHud(player, camera);
        overlay.addHudItem(gravityHud);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yymmdd_hhmmss");
        sessionName = dateFormat.format(new Date());
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

        // line highlighting
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
        overlay.draw(window.getWidth(), window.getHeight(), gl::getPositionOnScreen, activeCamera.getEye());

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
        overlay.removeHudItem(gravityHud);
    }
}
