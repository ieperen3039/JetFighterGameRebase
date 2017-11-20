package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Controllers.InputDelegate;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.io.IOException;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 * manages rendering, the gameloop and the rendering loop
 */
public abstract class GLFWGameEngine {

    protected AbstractGameLoop renderLoop;
    protected AbstractGameLoop gameLoop;

    protected GLFWWindow window;
    protected Camera camera;

    private boolean hudEnabled = true;

    public GLFWGameEngine() throws IOException {
        window = new GLFWWindow(Settings.GAME_NAME, 1600, 900, true);
        new InputDelegate(window);
        camera = new PointCenteredCamera();
    }

    /**
     * create a thread for everyone who wants one, open the main window and start the game
     * Rendering must happen in the main thread, so we do
     */
    public void startGame(){
        Thread gameLoopThread = new Thread(gameLoop);

        window.open();

        gameLoopThread.start();

        renderLoop.run();

        try {
            // release block
            gameLoop.unPause();
            gameLoopThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toolbox.print("Exiting engine! Bye ~");

        window.cleanup();
        // Finish execution
    }

    public void exitGame(){
        Toolbox.print("Exiting game: starting cleanup");
        window.close();
    }

    /**
     * Start closing and cleaning everything
     */
    public void cleanup() {

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
        return window.shouldClose();
    }
}
