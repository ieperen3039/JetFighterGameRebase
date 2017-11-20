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
            gameLoopThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.cleanup();
        window.cleanup();

        Toolbox.print("Game has stopped! Bye ~");
        // Finish execution
    }

    public void exitGame(){
        Toolbox.print("Stopping game...");
        window.close();
        gameLoop.stopLoop();
    }

    /**
     * Start closing and cleaning everything
     */
    public abstract void cleanup();

    /**
     * Get the {@link GLFWWindow} of the currently running instance.
     *
     * @return The currently active Window.
     */
    public GLFWWindow getWindow() {
        return window;
    }

    public boolean isStopping() {
        return window.shouldClose();
    }
}
