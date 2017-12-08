package nl.NG.Jetfightergame.Tools.Tracked;

/**
 * Created by Geert van Ieperen on 4-5-2017.
 */
public class TrackedFloat extends TrackedObject<Float> implements nl.NG.Jetfightergame.Tools.Differable {

    public TrackedFloat(Float current, Float previous) {
        super(current, previous);
    }

    public TrackedFloat(Float initial) {
        super(initial);
    }

    /**
         * updates the value by adding the parameter to the current value
         * @param addition the value that is added to the current. actual results may vary
         */
    public void addUpdate(Float addition) {
        update(current() + addition);
    }

    /**
     * @return the increase of the last updatePosition, defined as (current - previous)
     */
    public Float difference() {
        return current() - previous();
    }
}
