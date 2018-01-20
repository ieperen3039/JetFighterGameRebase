package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.InputHandling.InputDelegate;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.Managers.CameraManager;
import nl.NG.Jetfightergame.Engine.Managers.ControllerManager;
import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.util.Collection;

import static nl.NG.Jetfightergame.Engine.Managers.CameraManager.CameraImpl.FollowingCamera;
import static nl.NG.Jetfightergame.Engine.Managers.CameraManager.CameraImpl.PointCenteredCamera;

/**
 * @author Jorren Hendriks.
 * @author Geert van Ieperen
 * manages rendering, the gameloop and the rendering loop
 */
public abstract class GLFWGameEngine {

    protected final ControllerManager playerInput;

    protected GLFWWindow window;
    protected CameraManager camera;
    protected GameMode currentGameMode;

    /**
     * Classes that extend this engine should implement their own gameloop and rendering loop
     */
    public GLFWGameEngine() {
        window = new GLFWWindow(Settings.GAME_NAME, 1600, 900, true);
        new InputDelegate(window);
        playerInput = new ControllerManager();
        camera = new CameraManager();
    }

    /**
     * create a thread for everyone who wants one, open the main window and start the game
     * Rendering must happen in the main thread.
     */
    public void startGame(){
        window.open();

        secondaryGameLoops().forEach(Thread::start);

        try {
            renderingLoop().unPause();
            renderingLoop().run();

            for (AbstractGameLoop abstractGameLoop : secondaryGameLoops()) {
                abstractGameLoop.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
            renderingLoop().interrupt();
            secondaryGameLoops().forEach(Thread::interrupt);
        } finally {
            Toolbox.checkGLError();
            this.cleanUp();
            window.cleanup();
        }

        Toolbox.print("Game has stopped! Bye ~");
        // Finish execution
    }

    protected abstract AbstractGameLoop renderingLoop();

    protected abstract Collection<AbstractGameLoop> secondaryGameLoops();

    /** tells the renderloop to stop, renderloop must call back to clean up others */
    public void exitGame(){
        System.out.println();
        Toolbox.printFrom(2, "Stopping game...");

        new TimeOutTimer("Interrupt Threads", 1000, () -> {
            renderingLoop().interrupt();
            secondaryGameLoops().forEach(Thread::interrupt);
        }).start();
        new TimeOutTimer("Kill Threads", 1500, () -> System.exit(-1)).start();

        renderingLoop().stopLoop();
        secondaryGameLoops().forEach(AbstractGameLoop::stopLoop);
    }

    /**
     * Start closing and cleaning everything
     */
    public abstract void cleanUp();

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
        currentGameMode = GameMode.MENU_MODE;
        window.freePointer();
        camera.switchTo(PointCenteredCamera);
        secondaryGameLoops().forEach(AbstractGameLoop::pause);
    }

    public void setPlayMode() {
        currentGameMode = GameMode.PLAY_MODE;
        window.capturePointer();
        camera.switchTo(FollowingCamera);
        secondaryGameLoops().forEach(AbstractGameLoop::unPause);
    }

    public void setSpectatorMode(){
        currentGameMode = GameMode.SPECTATOR_MODE;
        window.freePointer();
        camera.switchTo(PointCenteredCamera);
        secondaryGameLoops().forEach(AbstractGameLoop::unPause);
    }

    public boolean isPaused() {
        return getCurrentGameMode() == GameMode.MENU_MODE;
    }

    public abstract AbstractJet getPlayer();

    public enum GameMode {
        PLAY_MODE, SPECTATOR_MODE, MENU_MODE
    }

    class TimeOutTimer extends Thread {
        private final String name;
        private final long millis;
        private final Runnable action;

        public TimeOutTimer(String name, long millis, Runnable action) {
            this.name = name;
            this.millis = millis;
            this.action = action;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
                Toolbox.print(name + " timed out!");
                action.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
