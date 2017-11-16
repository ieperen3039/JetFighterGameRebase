package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public class TrackedVector<V extends Vector> extends TrackedDifferable<V> {
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

    @Override
    public void addUpdate(Vector addition) {
        // unchecked cast, but once initialized on one type, it will always stay that type.
        // this works as long as Vector.add(vector) produces the same type as it was before
        update((V) current().add(addition));
    }
}
