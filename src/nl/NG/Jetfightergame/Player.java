package nl.NG.Jetfightergame;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;

/**
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public class Player {

    private ControllerManager input;
    /** health in percentage */
    private int health;

    private AbstractJet jet;

    public Player(ControllerManager input, AbstractJet jet) {
        this.input = input;
        this.jet = jet;
        health = 100;
    }

    public void switchController(ControllerManager.ControllerImpl type){
        input.switchTo(type);
    }

    public Controller getInput(){
        return input;
    }

    public AbstractJet jet() {
        return jet;
    }
}
