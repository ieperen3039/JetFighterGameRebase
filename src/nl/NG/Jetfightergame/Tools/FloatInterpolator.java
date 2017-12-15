package nl.NG.Jetfightergame.Tools;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class FloatInterpolator extends Interpolator<Float> {

    public FloatInterpolator(int capacity) {
        super(capacity);
    }

    @Override
    protected Float Interpolate(float timeStamp, double firstTime, Float firstElt, double secondTime, Float secondElt) {
        float fraction = (float) ((timeStamp - firstTime) / (secondTime - firstTime));
        float difference = secondElt - firstElt;

        return firstElt + (difference * fraction);
    }

}
