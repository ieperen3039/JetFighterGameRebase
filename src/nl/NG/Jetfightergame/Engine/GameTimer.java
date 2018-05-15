package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;

import static nl.NG.Jetfightergame.Settings.ClientSettings.RENDER_DELAY;

/**
 * a class that harbors a gameloop timer and a render timer, upon retrieving either of these timers, they are updated
 * with a modifiable in-game time.
 */
public class GameTimer {

    /** game-seconds since creating this gametimer */
    private float currentInGameTime;
    /** last record of system time */
    private long lastMark;
    /** multiplication factor to multiply system time units to game-seconds */
    private static final float MUL_TO_SECONDS = 1E-9f;

    private final TrackedFloat gameTime;
    private final TrackedFloat renderTime;
    private boolean isPaused = false;

    public GameTimer() {
        this(0f);
    }

    public GameTimer(float startTime) {
        currentInGameTime = startTime;
        gameTime = new TrackedFloat(startTime);
        renderTime = new TrackedFloat(startTime - RENDER_DELAY);
        lastMark = System.nanoTime();
    }

    public void updateGameTime(){
        updateTimer();
        gameTime.update(currentInGameTime);
    }

    public void updateRenderTime(){
        updateTimer();
        renderTime.update(currentInGameTime - RENDER_DELAY);
    }

    public TrackedFloat getGameTime(){
        return gameTime;
    }

    public TrackedFloat getRenderTime(){
        return renderTime;
    }

    /** returns the current in-game time of this moment */
    public float time(){
        updateTimer();
        return currentInGameTime;
    }

    /** may be called anytime */
    private void updateTimer(){
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastMark) * MUL_TO_SECONDS;
        lastMark = currentTime;

        if (!isPaused) currentInGameTime += deltaTime;
    }

    /** stops the in-game time */
    public void pause(){
        updateTimer();
        isPaused = true;
    }

    /** lets the in-game time proceed, without jumping */
    public void unPause(){
        updateTimer();
        isPaused = false;
    }

    /**
     * @param offset the ingame time is offset by the given time
     */
    public void addOffset(float offset){
        currentInGameTime += offset;
    }

    /** sets the ingame time to the given time */
    public void set(float time){
        updateTimer();
        currentInGameTime = time;
    }
}
