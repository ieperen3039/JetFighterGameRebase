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

    private CountDownLatch pauseBlock = new CountDownLatch(0); //TODO find better way?
    private boolean isPaused;

    public AbstractGameLoop(int targetTps) {
        this.targetTps = targetTps;
    }

    /**
     * invoked (targetTps) times per second
     * @param deltaTime
     */
    protected abstract void update(float deltaTime) throws InterruptedException;

    /**
     * @return true if the game loop should end.
     * returning true will cause the gameloop to finish its run iff isPaused returns false
     */
    protected abstract boolean shouldStop();

    protected abstract void cleanup();

    /**
     * start the gameloop once game is unpaused, and never terminate.
     * wrap-up must end up in a finally bock
     */
    public void run() {
        Toolbox.print(getName() + " has started");
        Timer loopTimer = new Timer();
        float deltaTime = 0;
        try {
            pauseBlock.await();

            while (!shouldStop()) {
                // start measuring how long a gameloop takes
                loopTimer.updateLoopTime();

                // do stuff
                update(deltaTime);

                long remainingTime = (1000 / targetTps) - loopTimer.getTimeSinceLastUpdate();

                // print debug info (hit = to refresh)
                TPSMinimum.updateAndPrint(getName() + ":", 1000f / loopTimer.getElapsedTime(), "per second");
                if (remainingTime < 0) Toolbox.print(getName() + " can't keep up! Running " + -remainingTime + " milliseconds behind");

                // sleep at least one millisecond
                long correctedTime = Math.max(remainingTime, 1);
                Thread.sleep(correctedTime);

                loopTimer.updateLoopTime();
                // store the duration and set this as length of next update
                deltaTime = loopTimer.getElapsedSeconds();
                // wait if the game is paused
                pauseBlock.await();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(getName() + " has Crashed! Blame Menno.");
        } finally {
            cleanup();
        }

        // terminate engine
    }

    /**
     * @return a name that identifies this gameloop
     */
    protected String getName() {
        return this.getClass().getSimpleName();
    }

    public void unPause(){
        pauseBlock.countDown();
        Toolbox.print("unpaused " + getName());
    }

    public void pause(){
        pauseBlock = new CountDownLatch(1);
        Toolbox.print("paused " + getName());
    }

    public boolean isPaused() {
        return pauseBlock.getCount() != 0;
    }

    public void resetTPSCounter(){
        TPSMinimum.reset();
    }
}
