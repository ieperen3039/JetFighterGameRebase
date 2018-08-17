package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;

/**
 * @author Geert van Ieperen. Created on 2-7-2018.
 */
public interface Player {

    /**
     * @return a unique name of this player
     */
    String playerName();

    /**
     * @return the one and only entity belonging to this player
     */
    AbstractJet jet();

}
