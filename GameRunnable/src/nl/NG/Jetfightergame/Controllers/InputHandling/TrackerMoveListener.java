package nl.NG.Jetfightergame.Controllers.InputHandling;

/**
 * classes implementing this are listening to the mousetracker
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public interface TrackerMoveListener extends TrackerListener {
    /** event that fires when the mouse is moved deltaX pixels right and deltaY pixels down */
    void mouseMoved(int deltaX, int deltaY);
}
