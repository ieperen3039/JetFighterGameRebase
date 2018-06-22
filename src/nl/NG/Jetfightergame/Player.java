package nl.NG.Jetfightergame;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;

/**
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public class Player implements TemporalEntity {

    private Controller override;
    private Controller input;
    /** health in percentage */
    private int health;

    private AbstractJet jet;
    private boolean isEnabled;

    public Player(Controller input, AbstractJet jet) {
        this.input = input;
        this.jet = jet;
        override = new Controller.EmptyController();
        health = 100;
    }

    /**
     * allow the control of this player to be overridden
     * @param enabled If false, player has control.
     *                If true, controls are overridden.
     */
    public void setControl(boolean enabled){
        isEnabled = enabled;
    }

    public Controller getInput() {
        return isEnabled ? input : override;
    }

    public AbstractJet jet() {
        return jet;
    }

    @Override
    public ParticleCloud explode() {
        return Particles.splitIntoParticles(jet, 20);
    }

    public boolean isOverdue() {
        return health <= 0;
    }
}
