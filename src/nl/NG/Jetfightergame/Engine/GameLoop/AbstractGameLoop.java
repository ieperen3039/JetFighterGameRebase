package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.AveragingQueue;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;


/**
 * @author Geert van Ieperen
 * recreated on 29-10-2019
 *
 * a general-purpose game loop
 * also usable for rendering
 */
public abstract class AbstractGameLoop extends Thread {
    private final String loopName;

    private float targetDeltaMillis;
    private CountDownLatch pauseBlock = new CountDownLatch(0);
    private boolean shouldStop;
    private boolean isPaused = true;
    private final boolean notifyDelay;
    private Consumer<Exception> exceptionHandler;

    private AveragingQueue avgTPS;
    private AveragingQueue avgPoss;
    private final Consumer<ScreenOverlay.Painter> tickCounter;
    private final Consumer<ScreenOverlay.Painter> possessionCounter;

    public AbstractGameLoop(String name, int targetTps, boolean notifyDelay, Consumer<Exception> exceptionHandler) {
        this.targetDeltaMillis = 1000f/targetTps;
        this.notifyDelay = notifyDelay;
        this.exceptionHandler = exceptionHandler;
        loopName = name;
        avgTPS = new AveragingQueue(targetTps/2);
        avgPoss = new AveragingQueue(targetTps/10);
        tickCounter = (hud) -> hud.printRoll(String.format("%s TPS: %1.01f", name, avgTPS.average()));
        possessionCounter = (hud) -> hud.printRoll(String.format("%s POSS: %2.0f%%", name, 100* avgPoss.average()));
    }

    /**
     * invoked (targetTps) times per second
     * @param deltaTime real-time difference since last loop
     */
    protected abstract void update(float deltaTime) throws InterruptedException;

    /**
     * commands the engine to finish the current loop, and then quit
     */
    public void stopLoop(){
        shouldStop = true;
        pauseBlock.countDown();
    }

    protected abstract void cleanup();

    /**
     * start the loop, running until {@link #stopLoop()} is called.
     */
    public void run() {
        Toolbox.print(loopName + " enabled");
        Timer loopTimer = new Timer();
        float deltaTime = 0;

        ScreenOverlay.addHudItem(tickCounter);
        ScreenOverlay.addHudItem(possessionCounter);

        try {
            pauseBlock.await();
            isPaused = false;

            while (!shouldStop) {
                // start measuring how long a gameloop takes
                loopTimer.updateLoopTime();

                // do stuff
                update(deltaTime);

                if (Thread.interrupted()) break;

                // number of milliseconds remaining in this loop
                float remainingTime = targetDeltaMillis - loopTimer.getTimeSinceLastUpdate();
                if (Settings.DEBUG && notifyDelay && (remainingTime < 0))
                    System.err.printf("%s can't keep up! Running %1.01f milliseconds behind%n", loopName, -remainingTime);

                // sleep at least one millisecond
                long correctedTime = (long) Math.max(remainingTime, 1f);
                Thread.sleep(correctedTime);

                // store the duration and set this as length of next update
                loopTimer.updateLoopTime();
                deltaTime = loopTimer.getElapsedSeconds();

                // update Ticks per Second
                float realTPS = 1000f / loopTimer.getElapsedTime();
                avgTPS.add(realTPS);
                avgPoss.add(-(remainingTime - targetDeltaMillis) / targetDeltaMillis);

                // wait if the game is paused
                isPaused = true;
                pauseBlock.await();
                isPaused = false;
            }
        } catch (Exception ex) {
            System.err.println(loopName + " has Crashed! Blame Menno.");
            ex.printStackTrace();
            exceptionHandler.accept(ex);
        } finally {
            ScreenOverlay.removeHudItem(tickCounter);
            ScreenOverlay.removeHudItem(possessionCounter);
            cleanup();
        }

        // terminate engine
        Toolbox.print(loopName + " is stopped");
    }

    public void unPause(){
        pauseBlock.countDown();
        Toolbox.printFrom(2, "unpaused " + loopName);
    }

    public void pause(){
        pauseBlock = new CountDownLatch(1);
        Toolbox.printFrom(2, "paused " + loopName);
    }

    /**
     * @return true if this loop is not executing its loop.
     * This method returns false if {@link #pause()} is called, but the loop is still finishing its loop
     * @see #unPause()
     */
    public boolean isPaused() {
        return isPaused && (pauseBlock.getCount() > 0);
    }

    /** @deprecated */
    public void resetTPSCounter(){
    }

    public void setTPS(int TPS) {
        this.targetDeltaMillis = 1000f/TPS;
    }
}
