package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.Manager;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;

import java.util.Collection;

/**
 * @author Geert van Ieperen
 * created on 8-1-2018.
 *
 * possibly we make this an instance of GameState, empty the lists upon changing world, separate GameState from Environment
 * and reduce the Environment interface to #buildScene(). This removes a little overhead of the world instances.
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentManager.Worlds> {

    private final Player player;
    private final GameTimer time;
    private GameState instance;

    public EnvironmentManager(Player player, GameTimer time) {
        this.time = time;
        this.player = player;
        instance = new PlayerJetLaboratory(time);
        instance.buildScene(player);
    }

    @Override
    public void addEntity(GameEntity entity) {
        instance.addEntity(entity);
    }

    @Override
    public void addParticles(Collection<Particle> newParticles) {
        instance.addParticles(newParticles);
    }

    public enum Worlds {
        PlayerJetLaboratory,
        CollisionLaboratory,
        ExplosionLaboratory
    }

    @Override
    public void updateGameLoop() {
        instance.updateGameLoop();
    }

    @Override
    public void setLights(GL2 gl) {
        instance.setLights(gl);
    }

    @Override
    public void drawObjects(GL2 gl) {
        instance.drawObjects(gl);
    }

    @Override
    public void drawParticles(GL2 gl) {
        instance.drawParticles(gl);
    }

    @Override
    public GameTimer getTimer() {
        return instance.getTimer();
    }

    @Override
    public int getNumberOfLights() {
        return instance.getNumberOfLights();
    }


    @Override
    public Worlds[] implementations() {
        return Worlds.values();
    }

    @Override
    public void switchTo(Worlds implementation) {
        instance.cleanUp();
        Settings.SPECTATOR_MODE = false;

        switch (implementation) {
            case CollisionLaboratory:
                instance = new CollisionLaboratory(time);
                break;
            case PlayerJetLaboratory:
                instance = new PlayerJetLaboratory(time);
                break;
            case ExplosionLaboratory:
                instance = new ExplosionLaboratory(time);
                break;
            default:
                Toolbox.print("Environment not properly registered: " + implementation + " (did we forget a break statement?)");
                instance = new MissionMenu(time);
        }

        instance.buildScene(player);
    }

    /**
     * temporary replacement for in-game menu
     */
    private class MissionMenu extends GameState {
        public MissionMenu(GameTimer time) {
            super(time);
        }

        @Override
        public void buildScene(Player player) {
            dynamicEntities.add(player.jet());
        }

        @Override
        protected DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }
    }
}

