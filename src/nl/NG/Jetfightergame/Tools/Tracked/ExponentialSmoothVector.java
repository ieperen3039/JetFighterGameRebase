package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Vectors.Vector;

import static java.lang.StrictMath.pow;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class ExponentialSmoothVector<V extends Vector> extends TrackedVector<V> implements SmoothTracked<V> {

    private final float preservedFraction;

    /**
     * a vector that reduces its distance to its target with a preset fraction
     * @param preservedFraction the fraction that must be preserved per second
     */
    public ExponentialSmoothVector(V initial, float preservedFraction) {
        super(initial);
        this.preservedFraction = preservedFraction;
    }

    @Override
    public void updateFluent(V target, float deltaTime) {
        float deceleration = (float) pow(preservedFraction, deltaTime);
        addUpdate(current().to(target).scale(1 - deceleration));
    }
}
