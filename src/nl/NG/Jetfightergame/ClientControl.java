package nl.NG.Jetfightergame;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.GameState.Player;

/**
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public interface ClientControl extends Player {
    /**
     * allow the control of this player to be overridden
     * @param enabled If false, player has control.
     *                If true, controls are overridden.
     */
    void setControl(boolean enabled);

    Controller getInput();

    ControllerManager getInputControl();
}
