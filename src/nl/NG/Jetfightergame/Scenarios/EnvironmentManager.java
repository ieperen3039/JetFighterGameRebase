package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.Managers.Manager;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;

/**
 * @author Geert van Ieperen
 * created on 8-1-2018.
 *
 * possibly we make this an instance of GameState, empty the lists upon changing world, separate GameState from Environment
 * and reduce the Environment interface to #buildScene(). This removes a little overhead of the world instances.
 */
public class EnvironmentManager implements Environment, Manager<EnvironmentManager.Worlds> {

    private GameState instance;
    private Controller input;

    public EnvironmentManager(Controller input) {
        this.input = input;
        instance = new ExplosionLaboratory(input);
    }

    public enum Worlds {
        CollisionLaboratory,
        PlayerJetLaboratory,
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
    public void updateParticles() {
        instance.updateParticles();
    }

    @Override
    public AbstractJet getPlayer() {
        return instance.getPlayer();
    }

    @Override
    public GameTimer getTimer() {
        return instance.getTimer();
    }


    @Override
    public Worlds[] implementations() {
        return Worlds.values();
    }

    @Override
    public void switchTo(Worlds implementation) {
        instance.cleanUp();

        switch (implementation) {
            case CollisionLaboratory:
                instance = new CollisionLaboratory(input);
                break;
            case PlayerJetLaboratory:
                instance = new PlayerJetLaboratory(input);
                break;
            case ExplosionLaboratory:
                instance = new ExplosionLaboratory(input);
                break;
            default:
                Toolbox.print("Environment not properly installed: " + implementation + " (did we forget a break statement?)");
                instance = new MissionMenu(input);
        }

        instance.buildScene();
    }

    /**
     * temporary replacement for in-game menu
     */
    private class MissionMenu extends GameState {
        public MissionMenu(Controller input) {
            super(input);
        }

        @Override
        public void buildScene() {
            dynamicEntities.add(getPlayer());
        }

        @Override
        protected DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }
    }
}

