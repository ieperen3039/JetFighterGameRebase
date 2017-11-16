package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Tools.Differable;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public abstract class TrackedDifferable<T> extends TrackedObject<T> implements Differable {
    public TrackedDifferable(T current, T previous) {
        super(current, previous);
    }

    public TrackedDifferable(T initial) {
        super(initial);
    }

    /**
     * updates the value by adding the parameter to the current value
     * @param addition the value that is added to the current. actual results may vary
     */
    public abstract void addUpdate(T addition);
}
