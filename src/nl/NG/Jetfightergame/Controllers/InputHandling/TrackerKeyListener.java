package nl.NG.Jetfightergame.Controllers.InputHandling;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public interface TrackerKeyListener extends TrackerListener {
    /** actual implementation of the key depends on inputhandling */
    void keyPressed(int key);
}
