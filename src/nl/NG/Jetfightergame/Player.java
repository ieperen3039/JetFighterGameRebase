package nl.NG.Jetfightergame;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;

import static nl.NG.Jetfightergame.Controllers.ControllerManager.ControllerImpl.EmptyController;

/**
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public class Player {
    private final SubControl input;
    public final String name;
    private AbstractJet jet;

    public Player(String name, ClientConnection connection) {
        this.input = new SubControl(EmptyController, connection);
        this.input.switchTo(0);
        this.name = name;
    }

    /**
     * allow the control of this player to be overridden
     * @param enabled If false, player has control.
     *                If true, controls are overridden.
     */
    public void setControl(boolean enabled){
        if (enabled) {
            input.enable();
        } else {
            input.disable();
        }
    }

    public void setJet(AbstractJet jet) {
        this.jet = jet;
    }

    public Controller getInput() {
        return input;
    }

    public ControllerManager getInputControl() {
        return input;
    }

    public AbstractJet jet() {
        return jet;
    }

    public ParticleCloud explode() {
        return Particles.splitIntoParticles(jet, 20);
    }

    @Override
    public String toString() {
        return name;
    }

    private static class SubControl extends ControllerManager {
        private final ControllerImpl secondary;
        ControllerImpl active;

        public SubControl(ControllerImpl secondary, ClientConnection output) {
            super(null, output);
            switchTo(0);
            this.secondary = secondary;
        }

        @Override
        public void switchTo(ControllerImpl type) {
            active = type;
            super.switchTo(type);
        }

        public void disable() {
            super.switchTo(secondary);
        }

        public void enable() {
            switchTo(active);
        }
    }
}
