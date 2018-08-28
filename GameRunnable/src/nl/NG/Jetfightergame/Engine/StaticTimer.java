package nl.NG.Jetfightergame.Engine;

import static nl.NG.Jetfightergame.Settings.ClientSettings.RENDER_DELAY;

/**
 * adds a fixed timestamp every loop. this results in slowdown when collisions ramp up and results in deterministic behaviour
 * @author Geert van Ieperen
 * created on 17-1-2018.
 */
public class StaticTimer extends GameTimer {
    private final float deltaTime;

    public StaticTimer(float targetFPS) {
        deltaTime = 1f/targetFPS;
    }

    @Override
    public void updateGameTime() {
        currentInGameTime += deltaTime;
        gameTime.update(currentInGameTime);
    }

    @Override
    protected void updateTimer() {
    }

    @Override
    public void updateRenderTime() {
        renderTime.update(currentInGameTime - RENDER_DELAY);
    }
}
