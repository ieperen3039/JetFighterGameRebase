package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.GameState.SpawnReceiver;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public interface GameServer extends SpawnReceiver {

    /** pauses the server  */
    void pause();

    /** intiates the shutdown sequence of the server */
    void shutDown();

    /** starts the server if it is enabled */
    void unPause();
}