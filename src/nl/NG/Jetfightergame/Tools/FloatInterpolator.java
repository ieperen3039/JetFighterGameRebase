package nl.NG.Jetfightergame.Tools;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class FloatInterpolator extends Interpolator<Float> {

    public FloatInterpolator(int capacity, Float initialValue) {
        super(capacity, initialValue);
    }

    @Override
    protected Float interpolate(float timeStamp, double firstTime, Float firstElt, double secondTime, Float secondElt) {
        float fraction = (float) ((timeStamp - firstTime) / (secondTime - firstTime));
        float difference = secondElt - firstElt;

        return firstElt + (difference * fraction);
    }

}
