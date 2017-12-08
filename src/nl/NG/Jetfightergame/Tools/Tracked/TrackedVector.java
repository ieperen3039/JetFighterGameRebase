package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public class TrackedVector<V extends Vector> extends TrackedObject<V> implements nl.NG.Jetfightergame.Tools.Differable {
    public TrackedVector(V initial) {
        super(initial);
    }

    public TrackedVector(V current, V previous) {
        super(current, previous);
    }

    @Override
    public DirVector difference() {
        return (DirVector) previous().to(current());
    }

    /**
         * updates the value by adding the parameter to the current value
         * @param addition the value that is added to the current. actual results may vary
         */
    public void addUpdate(Vector addition) {
        // unchecked cast, but once initialized on one type, it will always stay that type.
        // this works as long as Vector.add(vector) produces the same type as it was before
        update((V) current().add(addition));
    }
}
