package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Vectors.DirVector;
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

    /**
     * @param target the target vector where this vector will move to.
     *               Note that this vector will be changed, supplying a new instance is advised
     * @param deltaTime the time since the last updatePosition, to allow speed and acceleration
     */
    @Override
    public void updateFluent(V target, float deltaTime) {
        float deceleration = (float) pow(preservedFraction, deltaTime);

        final DirVector movement = new DirVector();
        addUpdate(current().to(target, movement).scale(1 - deceleration, movement), target);
    }
}
