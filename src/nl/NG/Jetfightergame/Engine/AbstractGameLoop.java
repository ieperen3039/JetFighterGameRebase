package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.util.concurrent.CountDownLatch;


/**
 * @author Geert van Ieperen
 * recreated on 29-10-2019
 *
 * a general-purpose game loop
 * also usable for rendering
 */
public abstract class AbstractGameLoop implements Runnable {

    private final Extreme<Float> TPSMinimum = new Extreme<>(0f, false);
    private final int targetTps;
    private final String loopName;

    private CountDownLatch pauseBlock = new CountDownLatch(0); //TODO find better way?
    private boolean shouldStop;
    private final boolean notifyDelay;
    private Thread runningThread;

    public AbstractGameLoop(String name, int targetTps, boolean notifyDelay) {
        this.targetTps = targetTps;
        this.notifyDelay = notifyDelay;
        loopName = name;
    }

    /**
     * invoked (targetTps) times per second
     * @param deltaTime
     */
    protected abstract void update(float deltaTime) throws InterruptedException;

    /**
     * commands the engine to finish the current loop, and then quit
     */
    public void stopLoop(){
        shouldStop = true;
        unPause();
    }

    protected abstract void cleanup();

    /**
     * create a thread for this object, and start it
     */
    public void runThread(){
        runningThread = new Thread(this);
        runningThread.start();
    }

    /**
     * start the gameloop once game is unpaused, and never terminate.
     * wrap-up must end up in a finally bock
     */
    public void run() {
        Toolbox.print(loopName + " is started");
        Timer loopTimer = new Timer();
        float deltaTime = 0;
        try {
            pauseBlock.await();

            while (!shouldStop) {
                // start measuring how long a gameloop takes
                loopTimer.updateLoopTime();

                // do stuff
                update(deltaTime);

                long remainingTime = (1000 / targetTps) - loopTimer.getTimeSinceLastUpdate();
                if (Settings.DEBUG && notifyDelay && remainingTime < 0) Toolbox.print(loopName + " can't keep up! Running " + -remainingTime + " milliseconds behind");

                // sleep at least one millisecond
                long correctedTime = Math.max(remainingTime, 1);
                Thread.sleep(correctedTime);

                loopTimer.updateLoopTime();

                // print Ticks per Second
                TPSMinimum.updateAndPrint(loopName, 1000f / loopTimer.getElapsedTime(), "per second");

                // store the duration and set this as length of next update
                deltaTime = loopTimer.getElapsedSeconds();
                // wait if the game is paused
                pauseBlock.await();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(loopName + " has Crashed! Blame Menno.");
        } finally {
            cleanup();
        }

        // terminate engine
        Toolbox.print(loopName + " is stopped");
    }

    public void unPause(){
        pauseBlock.countDown();
        if (Settings.DEBUG) Toolbox.printFrom(2, "unpaused " + loopName);
    }

    public void pause(){
        pauseBlock = new CountDownLatch(1);
        if (Settings.DEBUG) Toolbox.printFrom(2, "paused " + loopName);
    }

    public boolean isPaused() {
        return pauseBlock.getCount() != 0;
    }

    public void resetTPSCounter(){
        TPSMinimum.reset();
    }

    public void waitForExit() throws InterruptedException {
        runningThread.join();
    }
}
