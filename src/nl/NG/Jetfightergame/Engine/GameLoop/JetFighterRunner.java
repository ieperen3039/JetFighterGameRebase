package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.Settings;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class JetFighterRunner extends AbstractGameLoop {

    private final GameState game;
    private float totalTime = 0;
    /** allows the game to slow down or speed up. with timeMultiplier = 2, the game goes twice as fast */
    private float timeMultiplier = 1f;

    public JetFighterRunner(GameState game) {
        super("GameEngine loop", Settings.TARGET_TPS, true);
        this.game = game;
    }

    @Override
    protected void update(float deltaTime) {
        totalTime += deltaTime * timeMultiplier;
        try {
            game.updateGameLoop(deltaTime, totalTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cleanup() {

    }
}
