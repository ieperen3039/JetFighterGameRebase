package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Engine.Window.GLFWWindow;
import nl.NG.Jetfightergame.Engine.Window.InputDelegate;
import nl.NG.Jetfightergame.ScreenOverlay.Hud;

import java.io.IOException;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 * manages rendering, the gameloop and the rendering loop
 */
public abstract class GLFWGameEngine {

    protected GameLoop renderLoop;
    protected GameLoop gameLoop;

    protected GLFWWindow window;
    private Hud hud;
    protected Camera camera;
    private boolean isStopping = false;

    private boolean hudEnabled = true;

    public GLFWGameEngine() throws IOException {
        window = new GLFWWindow(Settings.GAME_NAME, 1600, 900, true);
        new InputDelegate(window);
        hud = new Hud(window);
        camera = new PointCenteredCamera();
    }

    public void startGame(){
        //TODO make threads and wait
    }

    /**
     * Cleanup used objects and stop the game.
     * last in the call routine
     */
    public void exitGame() {
        isStopping = true;
        // wait for all threads

        window.cleanup();
    }

    /**
     * Get the {@link GLFWWindow} of the currently running instance.
     *
     * @return The currently active Window.
     */
    public GLFWWindow getWindow() {
        return window;
    }

    public void toggleHud() {
        hudEnabled = !hudEnabled;
    }

    public boolean isStopping() {
        return isStopping;
    }
}
