package nl.NG.Jetfightergame.Rendering.Interpolation;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class FloatInterpolator extends LinearInterpolator<Float> {

    public FloatInterpolator(int capacity, Float initialValue) {
        super(capacity, initialValue);
    }

    @Override
    protected Float interpolate(Float firstElt, Float secondElt, float fraction) {
        float difference = secondElt - firstElt;

        return firstElt + (difference * fraction);
    }

}
