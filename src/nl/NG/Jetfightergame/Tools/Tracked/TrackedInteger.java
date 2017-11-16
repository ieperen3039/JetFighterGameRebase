package nl.NG.Jetfightergame.Tools.Tracked;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 */
public class TrackedInteger extends TrackedDifferable<Integer> {
    public TrackedInteger(Integer current, Integer previous) {
        super(current, previous);
    }

    public TrackedInteger(Integer initial) {
        super(initial);
    }

    @Override
    public void addUpdate(Integer addition) {
        update(current() + addition);
    }

    @Override
    public Integer difference() {
        return current() - previous();
    }
}
