package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;

/**
 * adds a fixed timestamp every loop. this results in slowdown when collisions ramp up and results in deterministic behaviour
 * @author Geert van Ieperen
 * created on 17-1-2018.
 */
public class StaticTimer extends GameTimer {
    private TrackedFloat currentTime = new TrackedFloat(0f);
    private TrackedFloat renderTime = new TrackedFloat(-Settings.RENDER_DELAY);
    private final float deltaTime;
    private float gameMult = 1f;
    private float engineMult = 1f;

    public StaticTimer(float targetFPS) {
        deltaTime = 1f/targetFPS;
    }

    @Override
    public void updateGameTime() {
        currentTime.addUpdate(deltaTime * gameMult * engineMult);
    }

    @Override
    public void updateRenderTime() {
        renderTime.update(currentTime.current() - Settings.RENDER_DELAY);
    }

    @Override
    public TrackedFloat getGameTime() {
        return currentTime;
    }

    @Override
    public TrackedFloat getRenderTime() {
        return renderTime;
    }

    @Override
    public float time() {
        return currentTime.current();
    }

    @Override
    public void setGameTimeMultiplier(float multiplier) {
        gameMult = multiplier;
    }

    @Override
    public void setEngineMultiplier(float multiplier) {
        engineMult = multiplier;
    }
}
