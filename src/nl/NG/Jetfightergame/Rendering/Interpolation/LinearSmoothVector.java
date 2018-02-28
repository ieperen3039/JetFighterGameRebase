package nl.NG.Jetfightergame.Rendering.Interpolation;

import nl.NG.Jetfightergame.Tools.Tracked.SmoothTracked;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 28-2-2018.
 */
public class LinearSmoothVector<V extends Vector> extends TrackedVector<V> implements SmoothTracked<V> {
    private final float catchUp;

    public LinearSmoothVector(V initial, float catchUp) {
        super(initial);
        this.catchUp = catchUp;
    }

    @Override
    public void updateFluent(V target, float deltaTime) {
        final DirVector direction = current().to(target, new DirVector());
        final float gain = deltaTime * catchUp;

        if (direction.length() < gain) update(target);

        target.add(direction.reducedTo(gain, direction), target);
        update(target);
    }
}
