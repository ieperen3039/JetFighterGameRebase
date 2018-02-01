package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
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

    private final Extreme<Float> TPSMinimum = new Extreme<>(0f, false);
    private final String loopName;

    private int targetTps;
    private CountDownLatch pauseBlock = new CountDownLatch(0);
    private boolean shouldStop;
    private boolean isPaused = true;
    private final boolean notifyDelay;
    private Consumer<Exception> exceptionHandler;

    private Queue<Float> avgTPS;
    private final Consumer<ScreenOverlay.Painter> tickCounter;

    public AbstractGameLoop(String name, int targetTps, boolean notifyDelay, Consumer<Exception> exceptionHandler) {
        this.targetTps = targetTps;
        this.notifyDelay = notifyDelay;
        this.exceptionHandler = exceptionHandler;
        loopName = name;
        avgTPS = new ArrayBlockingQueue<>(targetTps/2);
        tickCounter = (hud) ->
                hud.printRoll(String.format("%s: %1.01f", name, avgTPS.stream().mapToDouble(Float::doubleValue).average().orElse(0)));
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
     * start the loop, running until {@link #stopLoop()} is called
     * wrap-up must end up in a finally bock
     */
    public void run() {
        Toolbox.print(loopName + " enabled");
        Timer loopTimer = new Timer();
        float deltaTime = 0;

        ScreenOverlay.addHudItem(tickCounter);

        try {
            pauseBlock.await();
            isPaused = false;

            while (!shouldStop) {
                // start measuring how long a gameloop takes
                loopTimer.updateLoopTime();

                // do stuff
                update(deltaTime);

                if (Thread.interrupted()) break;

                long remainingTime = (1000 / targetTps) - loopTimer.getTimeSinceLastUpdate();
                if (Settings.DEBUG && notifyDelay && (remainingTime < 0))
                    System.err.println(loopName + " can't keep up! Running " + -remainingTime + " milliseconds behind");

                // sleep at least one millisecond
                long correctedTime = Math.max(remainingTime, 1);
                Thread.sleep(correctedTime);

                loopTimer.updateLoopTime();

                // print Ticks per Second
                float realTPS = 1000f / loopTimer.getElapsedTime();
                TPSMinimum.updateAndPrint(loopName, realTPS, "per second");
                while (avgTPS.size() >= (targetTps / 2)) avgTPS.remove();
                avgTPS.offer(realTPS);

                // store the duration and set this as length of next update
                deltaTime = loopTimer.getElapsedSeconds();
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

    public void resetTPSCounter(){
        TPSMinimum.reset();
    }

    public void setTPS(int TPS) {
        this.targetTps = TPS;
    }
}
