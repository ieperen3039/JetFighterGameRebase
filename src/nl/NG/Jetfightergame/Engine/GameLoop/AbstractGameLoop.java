package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Tools.Extreme;
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

    private final Extreme<Float> TPSMinimum = new Extreme<>(0f, false);
    private final String loopName;

    private int targetTps;
    private CountDownLatch pauseBlock = new CountDownLatch(0); //TODO find better way?
    private boolean shouldStop;
    private final boolean notifyDelay;
    private Consumer<Exception> exceptionHandler;

    public AbstractGameLoop(String name, int targetTps, boolean notifyDelay, Consumer<Exception> exceptionHandler) {
        this.targetTps = targetTps;
        this.notifyDelay = notifyDelay;
        this.exceptionHandler = exceptionHandler;
        loopName = name;
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
        unPause();
    }

    protected abstract void cleanup();

    /**
     * start the gameloop once game is unpaused, and never terminate.
     * wrap-up must end up in a finally bock
     */
    public void run() {
        Toolbox.print(loopName + " enabled");
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
                if (Settings.DEBUG && notifyDelay && remainingTime < 0)
                    System.err.println(loopName + " can't keep up! Running " + -remainingTime + " milliseconds behind");

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

    public boolean isPaused() {
        return pauseBlock.getCount() != 0;
    }

    public void resetTPSCounter(){
        TPSMinimum.reset();
    }

    public void setTPS(int TPS) {
        this.targetTps = TPS;
    }
}
