package nl.NG.Jetfightergame.Tools.Interpolation;

import nl.NG.Jetfightergame.Settings.ClientSettings;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class QuaternionInterpolator extends LinearInterpolator<Quaternionf> {

    private static final float DOT_THRESHOLD = 0.125f;

    /**
     * @param capacity    the initial expected maximum number of entries
     * @param initialItem
     */
    public QuaternionInterpolator(int capacity, Quaternionf initialItem) {
        super(capacity, initialItem);
    }

    @Override
    protected Quaternionf interpolate(Quaternionf firstElt, Quaternionf secondElt, float fraction) {
        final Quaternionf result = new Quaternionf();

        if (ClientSettings.ITERATIVE_ROTATION_INTERPOLATION && firstElt.dot(secondElt) > DOT_THRESHOLD) {
            firstElt.nlerpIterative(secondElt, fraction, DOT_THRESHOLD, result);
        } else {
            firstElt.nlerp(secondElt, fraction, result);
        }
        return result;
    }

    @Override
    public Quaternionf getDerivative() {
        Quaternionf dx = activeElement.difference(nextElement(), new Quaternionf());
        float dy = getTimeDifference();
        return dx.scale(1 / dy);
    }
}
