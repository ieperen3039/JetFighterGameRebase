package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Tools.Logger;

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

    /** change the current map to the previousy given race map */
    void startRace();

    @Override
    default void addParticles(ParticleCloud particles) {
        Logger.WARN.print("tried adding particles while running headless");
    }
}
