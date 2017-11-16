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
public abstract class GameLoop implements Runnable {

    public final Timer loopTimer;
    private final Extreme<Float> TPSMinimum = new Extreme<>(0f, false);
    private final int targetTps;

    private CountDownLatch pauseBlock = new CountDownLatch(1); //TODO find better way?

    public GameLoop(int targetTps) {
        loopTimer = new Timer();
        this.targetTps = targetTps;
    }

    /**
     * invoked (targetTps) times per second
     * @param deltaTime
     */
    protected abstract void update(float deltaTime);

    /**
     * start the gameloop once game is unpaused, and never terminate.
     * wrap-up must end up in a finally bock
     */
    public void run() {
        try {
            while (!shouldStop()) {
                // block if the game is paused
                pauseBlock.await();

                loopTimer.updateLoopTime();

                // do stuff
                update(loopTimer.getElapsedSeconds());

                long remainingTime = (1000 / targetTps) - loopTimer.getTimeSinceLastUpdate();

                // print debug info (hit = to refresh)
                TPSMinimum.updateAndPrint(this.getClass().getSimpleName() + ":", 1000f / loopTimer.getElapsedTime(), "per second");
                if (remainingTime < 0) Toolbox.print("Can't keep up! Running " + -remainingTime + " milliseconds behind");

                // sleep at least one millisecond
                long correctedTime = Math.max(remainingTime, 1);

                Thread.sleep(correctedTime);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("The Gameloop has Crashed! Blame Menno.");
        }
        // terminate engine
    }

    /**
     * @return true if the game loop should end.
     * returning true will cause the gameloop to finish its run iff isPaused returns false
     */
    protected abstract boolean shouldStop();

    public void unPause(){
        pauseBlock.countDown();
        Toolbox.print("unpaused game");
    }

    public void pause(){
        pauseBlock = new CountDownLatch(1);
        Toolbox.print("paused game");
    }

    public boolean isPaused() {
        return pauseBlock.getCount() != 0;
    }

    public void resetTPSCounter(){
        TPSMinimum.reset();
    }
}
