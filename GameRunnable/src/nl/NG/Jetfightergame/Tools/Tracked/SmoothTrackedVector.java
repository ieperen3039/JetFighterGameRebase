package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen. Created on 10-7-2018.
 */
public abstract class SmoothTrackedVector<V extends Vector> extends TrackedVector<V> implements SmoothTracked<V> {
    public SmoothTrackedVector(V initial) {
        super(initial);
    }

    public SmoothTrackedVector(V previous, V current) {
        super(previous, current);
    }
}
