package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Camera.FollowingCamera;
import nl.NG.Jetfightergame.Camera.PointCenteredCamera;
import nl.NG.Jetfightergame.Controllers.InputDelegate;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;

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
    protected GameMode currentGameMode;

    /**
     * Classes that extend this engine should implement their own gameloop and rendering loop
     * @throws IOException
     */
    public GLFWGameEngine() throws IOException {
        window = new GLFWWindow(Settings.GAME_NAME, 1600, 900, true);
        new InputDelegate(window);
        camera = new PointCenteredCamera();
    }

    /**
     * create a thread for everyone who wants one, open the main window and start the game
     * Rendering must happen in the main thread.
     */
    public void startGame(){
        Toolbox.print("Initialisation complete\n");
        window.open();

        gameLoop.start();

        try {
            renderLoop.run();
            gameLoop.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.cleanup();
            window.cleanup();
        }

        Toolbox.print("Game has stopped! Bye ~");
        // Finish execution
    }

    /** tells the renderloop to stop, renderloop must call back to clean up others */
    public void exitGame(){ // TODO add timer for forced shutdown
        System.out.println();
        Toolbox.print("Stopping game...");
        renderLoop.stopLoop();
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

    public GameMode getCurrentGameMode() {
        return currentGameMode;
    }

    public void setMenuMode() {
        // TODO set cursor visibility
        this.currentGameMode = GameMode.MENU_MODE;
        window.freePointer();
        camera = new PointCenteredCamera(getPlayer().getPosition(), DirVector.Z, 1, 1);
        gameLoop.pause();
    }

    public void setPlayMode() {
        // TODO set cursor visibility
        this.currentGameMode = GameMode.PLAY_MODE;
        window.capturePointer();
        camera = new FollowingCamera(camera.getEye(), getPlayer());
        gameLoop.unPause();
    }

    public boolean isPaused() {
        return getCurrentGameMode() == GameMode.MENU_MODE;
    }

    public abstract AbstractJet getPlayer();

    public enum GameMode {
        PLAY_MODE, MENU_MODE
    }
}
