package nl.NG.Jetfightergame.Engine;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class JetFighterRunner extends AbstractGameLoop {

    private final JetFighterGame game;

    public JetFighterRunner(JetFighterGame game) {
        super(Settings.TARGET_TPS);
        this.game = game;
    }

    @Override
    protected String getName() {
        return "the Gameloop";
    }

    @Override
    protected void update(float deltaTime) {
        game.updateGameLoop(deltaTime);
    }

    @Override
    protected boolean shouldStop() {
        return game.isStopping();
    }

    @Override
    protected void cleanup() {

    }
}
