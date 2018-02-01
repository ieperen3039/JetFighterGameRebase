package nl.NG.Jetfightergame;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;

import java.util.Collection;

/**
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public class Player implements MortalEntity {

    private ControllerManager input;
    /** health in percentage */
    private int health;

    private AbstractJet jet;

    public Player(ControllerManager input, AbstractJet jet) {
        this.input = input;
        this.jet = jet;
        health = 100;
    }

    public void switchController(ControllerManager.ControllerImpl type) {
        input.switchTo(type);
    }

    public Controller getInput() {
        return input;
    }

    public AbstractJet jet() {
        return jet;
    }

    @Override
    public Collection<Particle> explode() {
        return jet.explode();
    }

    public boolean isDead() {
        return health <= 0;
    }
}
