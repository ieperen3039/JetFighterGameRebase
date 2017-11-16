package nl.NG.Jetfightergame.Controllers;

/**
 * classes implementing this are listening to the mousetracker
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public interface TrackerMoveListener {
    /**
     * this method is called based on {@link java.awt.event.MouseEvent}
     * @param deltaX horizontal difference in position since the last tick.
     * @param deltaY vertical positional difference since the last tick.
     */
    void mouseMoved(int deltaX, int deltaY);
}
