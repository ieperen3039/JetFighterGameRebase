package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.PowerupType;

/**
 * @author Geert van Ieperen. Created on 2-7-2018.
 */
public interface Player {

    /**
     * adds the given primitive to the current powerup
     * @param type the added primitive
     * @return the new powerup.
     */
    default PowerupType currentWith(PowerupType.Primitive type) {
        PowerupType current = getCurrentPowerup();
        return current == null ? PowerupType.get(type) : current.with(type);
    }

    /**
     * @return a unique name of this player
     */
    String playerName();

    /**
     * @return the one and only entity belonging to this player
     */
    AbstractJet jet();

    /**
     * @return the current combination of powerups
     */
    PowerupType getCurrentPowerup();

    /**
     * adds one of the given type to the player's current powerup
     * @return true iff the powerup is accepted by the player
     */
    boolean addPowerup(PowerupType.Primitive type);
}
