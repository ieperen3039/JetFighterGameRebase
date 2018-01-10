package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;

import static nl.NG.Jetfightergame.Engine.Settings.RENDER_DELAY;

/**
 * a class that harbors a gameloop timer and a render timer, upon retrieving either of these timers, they are updated
 * with a modifiable in-game time.
 */
public class GameTimer {

    /** multiplication of time as effect of in-game events. with playMultiplier = 2, the game goes twice as fast. */
    private float playMultiplier = 1f;
    /** multiplication of time by the engine. Main use is for pausing. with engineMultiplier = 2, the game goes twice as fast. */
    private float engineMultiplier;

    /** game-seconds since creating this gametimer */
    private float currentInGameTime;
    /** last record of system time */
    private long lastMark;
    /** multiplication factor to multiply system time units to game-seconds */
    private static final float MUL_TO_SECONDS = 1E-9f;

    private final TrackedFloat gameTime;
    private final TrackedFloat renderTime;

    GameTimer() {
        currentInGameTime = 0f;
        gameTime = new TrackedFloat(0f);
        renderTime = new TrackedFloat(-RENDER_DELAY);
        lastMark = System.nanoTime();
    }

    private void updateGameTime(){
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
        currentInGameTime += deltaTime * playMultiplier * engineMultiplier;
    }

    /**
     * @param multiplier time will move {@code multiplier} times as fast
     */
    public void setGameTimeMultiplier(float multiplier) {
        playMultiplier = multiplier;
    }

    /**
     * @param multiplier time will move {@code multiplier} times as fast
     */
    public void setEngineMultiplier(float multiplier){
        engineMultiplier = multiplier;
    }
}
