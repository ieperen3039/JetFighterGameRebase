package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.AveragingQueue;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Toolbox;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;


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

    private AveragingQueue avgTPS;
    private AveragingQueue avgPoss;
    private final Supplier<String> tickCounter;
    private final Supplier<String> possessionCounter;

    /**
     * creates a new, paused gameloop
     * @param name the name as displayed in {@link #toString()}
     * @param targetTps the target number of executions of {@link #update(float)} per second
     * @param notifyDelay if true, an error message will be printed whenever the update method has encountered delay.
     */
    public AbstractGameLoop(String name, int targetTps, boolean notifyDelay) {
        this.targetDeltaMillis = 1000f/targetTps;
        this.notifyDelay = notifyDelay;
        this.loopName = name;

        avgTPS = new AveragingQueue(targetTps/2);
        avgPoss = new AveragingQueue(targetTps/10);

        tickCounter = () -> String.format("%s TPS: %1.01f", name, avgTPS.average());
        possessionCounter = () -> String.format("%s POSS: %3d%%", name, (int) (100* avgPoss.average()));
    }

    /**
     * invoked (targetTps) times per second
     * @param deltaTime real-time difference since last loop
     */
    protected abstract void update(float deltaTime) throws Exception;

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
        if (ServerSettings.DEBUG) Toolbox.print(loopName + " enabled");
        float deltaTime = 0;

        Toolbox.printOnline(tickCounter);
        Toolbox.printOnline(possessionCounter);

        try {
            pauseBlock.await();
            Timer loopTimer = new Timer();
            isPaused = false;

            while (!shouldStop) {
                // start measuring how long a gameloop takes
                loopTimer.updateLoopTime();

                // do stuff
                update(deltaTime);

                if (Thread.interrupted()) break;

                // number of milliseconds remaining in this loop
                float remainingTime = targetDeltaMillis - loopTimer.getTimeSinceLastUpdate();
                if (ServerSettings.DEBUG && notifyDelay && (remainingTime < 0))
                    System.err.printf("%s can't keep up! Running %d milliseconds behind%n", loopName, (int) -remainingTime);

                // sleep at least one millisecond
                long correctedTime = (long) Math.max(remainingTime, 1f);
                Thread.sleep(correctedTime);

                // store the duration and set this as length of next update
                loopTimer.updateLoopTime();
                deltaTime = loopTimer.getElapsedSeconds();

                // update Ticks per Second
                float realTPS = 1000f / loopTimer.getElapsedTime();
                avgTPS.add(realTPS);
                avgPoss.add((targetDeltaMillis - remainingTime) / targetDeltaMillis);

                // wait if the game is paused
                isPaused = true;
                pauseBlock.await();
                isPaused = false;
            }
        } catch (Exception ex) {
            System.err.println(loopName + " has Crashed! Blame Menno.");
            ex.printStackTrace();
            exceptionHandler(ex);
        } finally {
            Toolbox.removeOnlineUpdate(tickCounter);
            Toolbox.removeOnlineUpdate(possessionCounter);
            cleanup();
        }

        // terminate engine
        Toolbox.print(loopName + " is stopped");
    }

    /**
     * is executed after printing the stacktrace
     * @param ex the exception that caused the crash
     */
    protected void exceptionHandler(Exception ex){}

    @Override
    public String toString() {
        return String.format("%s @%.01fTPS", loopName, avgTPS.average());
    }

    public void unPause(){
        pauseBlock.countDown();
        if (ServerSettings.DEBUG) Toolbox.printFrom(2, "unpaused " + loopName);
    }

    public void pause(){
        pauseBlock = new CountDownLatch(1);
        if (ServerSettings.DEBUG) Toolbox.printFrom(2, "paused " + loopName);
    }

    /**
     * @return true if this loop is not executing its loop.
     * This method returns false if {@link #pause()} is called, but the loop is still finishing its loop
     * @see #unPause()
     */
    public boolean isPaused() {
        return isPaused && (pauseBlock.getCount() > 0);
    }

    public void setTPS(int TPS) {
        this.targetDeltaMillis = 1000f/TPS;
    }
}
