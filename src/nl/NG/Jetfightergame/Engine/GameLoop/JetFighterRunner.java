package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.Settings;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class JetFighterRunner extends AbstractGameLoop {

    private final GameState game;

    public JetFighterRunner(GameState game) {
        super("GameEngine loop", Settings.TARGET_TPS, true);
        this.game = game;
    }

    @Override
    protected void update(float deltaTime) {
        game.updateGameLoop();
//            Toolbox.print("\n" + game.getPlayer());
    }

    @Override
    public void unPause() {
        game.time.setEngineMultiplier(1f);
        super.unPause();
    }

    @Override
    public void pause() {
        game.time.setEngineMultiplier(0f);
        super.pause();
    }

    @Override
    protected void cleanup() {

    }
}
