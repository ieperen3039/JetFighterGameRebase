package nl.NG.Jetfightergame.Tools.Tracked;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 */
public class TrackedInteger extends TrackedObject<Integer> implements nl.NG.Jetfightergame.Tools.Differable {
    public TrackedInteger(Integer current, Integer previous) {
        super(current, previous);
    }

    public TrackedInteger(Integer initial) {
        super(initial);
    }

    /**
         * updates the value by adding the parameter to the current value
         * @param addition the value that is added to the current. actual results may vary
         */
    public void addUpdate(Integer addition) {
        update(current() + addition);
    }

    @Override
    public Integer difference() {
        return current() - previous();
    }
}
