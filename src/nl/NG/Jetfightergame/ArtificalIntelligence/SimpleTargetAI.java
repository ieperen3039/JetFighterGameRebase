package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.GameState.Environment;

/**
 * @author Geert van Ieperen
 * created on 9-2-2018.
 */
public class SimpleTargetAI extends AI {
    private Environment game;

    public SimpleTargetAI(String name, int targetTps, Environment game) {
        super(name, targetTps);
        this.game = game;
    }

    @Override
    public void update() {

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
