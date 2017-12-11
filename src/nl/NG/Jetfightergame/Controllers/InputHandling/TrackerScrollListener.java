package nl.NG.Jetfightergame.Controllers.InputHandling;

/**
 * @author Geert van Ieperen
 *         created on 10-11-2017.
 */
public interface TrackerScrollListener extends TrackerListener {
    /**
     * invoked when the mouse scrolled.
     *
     */
    void mouseWheelMoved(float pixels);
}
