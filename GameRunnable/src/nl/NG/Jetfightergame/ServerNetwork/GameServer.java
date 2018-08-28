package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.GameState.SpawnReceiver;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface GameServer extends SpawnReceiver {

    /** pauses the server  */
    void pause();

    /** starts the server if it is enabled */
    void unPause();

    /** intiates the shutdown sequence of the server */
    void shutDown();

    /** change the current map to the previousy given race map */
    void startRace();
}
