package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Engine.GameState;

/**
 * @author Geert van Ieperen
 * created on 9-2-2018.
 */
public class DogeBallAI extends AI {
    private GameState game;

    public DogeBallAI(String name, int targetTps, GameState game) {
        super(name, targetTps);
        this.game = game;
    }

    @Override
    public float throttle() {
        return 0;
    }

    @Override
    public float pitch() {
        return 0;
    }

    @Override
    public float yaw() {
        return 0;
    }

    @Override
    public float roll() {
        return 0;
    }

    @Override
    public boolean primaryFire() {
        return false;
    }

    @Override
    public boolean secondaryFire() {
        return false;
    }

    @Override
    protected void update(float deltaTime) {
        
    }

    @Override
    protected void cleanup() {

    }
}
