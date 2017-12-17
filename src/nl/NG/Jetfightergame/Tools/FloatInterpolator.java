package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class FloatInterpolator extends Interpolator<Float> {

    public FloatInterpolator(int capacity, Float initialValue) {
        super(capacity, initialValue);
    }

    @Override
    protected Float interpolate(float timeStamp, double firstTime, Float firstElt, double secondTime, Float secondElt, Vector dest) {
        float fraction = (float) ((timeStamp - firstTime) / (secondTime - firstTime));
        float difference = secondElt - firstElt;

        return firstElt + (difference * fraction);
    }

}
