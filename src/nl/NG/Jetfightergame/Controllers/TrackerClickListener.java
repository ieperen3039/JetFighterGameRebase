package nl.NG.Jetfightergame.Controllers;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 */
public interface TrackerClickListener {
    /**
     * upon left-clicking, this fires in the event-loop
     * @param x horizontal screen position of mouse
     * @param y vertical screen position of mouse
     */
    void clickEvent(int x, int y);
}
