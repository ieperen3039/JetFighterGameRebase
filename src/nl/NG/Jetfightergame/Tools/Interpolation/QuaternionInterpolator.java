package nl.NG.Jetfightergame.Tools.Interpolation;

import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class QuaternionInterpolator extends LinearInterpolator<Quaternionf> {

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
        firstElt.nlerp(secondElt, fraction, result);
        return result;
    }
}
