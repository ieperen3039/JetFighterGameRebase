package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;

/**
 * @author Geert van Ieperen. Created on 2-7-2018.
 */
public interface Player {
    /**
     * @return a unique name of this player
     */
    String playerName();

    AbstractJet jet();
}
