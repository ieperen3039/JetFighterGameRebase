package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Engine.Settings;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class JetFighterRunner extends AbstractGameLoop {

    private final JetFighterGame game;

    public JetFighterRunner(JetFighterGame game) {
        super("GameEngine loop", Settings.TARGET_TPS, true);
        this.game = game;
    }

    @Override
    protected void update(float deltaTime) {
        game.updateGameLoop(deltaTime);
    }

    @Override
    protected void cleanup() {

    }
}
