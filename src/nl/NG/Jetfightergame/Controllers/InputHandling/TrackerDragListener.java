package nl.NG.Jetfightergame.Controllers.InputHandling;

/**
 * @author Geert van Ieperen
 *         created on 12-11-2017.
 */
public interface TrackerDragListener extends TrackerListener {

    /** event that fires when the mouse is moved deltaX pixels right and deltaY pixels down while left-clicked */
    void mouseDragged(int deltaX, int deltaY);
}
