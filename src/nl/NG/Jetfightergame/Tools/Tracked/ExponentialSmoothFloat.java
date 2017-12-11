package nl.NG.Jetfightergame.Tools.Tracked;

import static java.lang.StrictMath.pow;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public class ExponentialSmoothFloat extends TrackedFloat implements SmoothTracked<Float> {

    protected final float preservedFraction;

    /**
     * a float that reduces its distance to its target with a preset fraction
     * @param preservedFraction the fraction that must be preserved per second
     */
    public ExponentialSmoothFloat(float initial, float preservedFraction) {
        super(initial);
        this.preservedFraction = preservedFraction;
    }

    /**
     * a float that reduces its distance to its target with a preset fraction
     * @param preservedFraction the fraction that must be preserved per second
     */
    public ExponentialSmoothFloat(float current, float previous, float preservedFraction) {
        super(current, previous);
        this.preservedFraction = preservedFraction;
    }

    /**
     * @param current the current value
     * @param target the target value
     * @param preservedFraction the fraction of the difference that must be preserved per timeunit
     * @param deltaTime number of timeunits
     * @return a value that gives an exponential reduction towards target, when compared against deltatime
     */
    public static float fractionOf(float current, float target, float preservedFraction, float deltaTime) {
        float deceleration = (float) pow(preservedFraction, deltaTime);

        // non-absolute difference between current float and target float. negative if source moves down
        return deceleration * (target - current);
    }

    @Override
    public void updateFluent(Float target, float deltaTime) {
        if (deltaTime == 0){
            return;
        }
        update(fractionOf(current(), target, preservedFraction, deltaTime));
    }
}
