package nl.NG.Jetfightergame.Tools.Interpolation;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class VectorInterpolator extends LinearInterpolator<Vector> {

    public VectorInterpolator(int capacity, Vector initialValue) {
        super(capacity, initialValue);
    }

    protected Vector interpolate(Vector firstElt, Vector secondElt, float fraction) {
        return firstElt.interpolateTo(secondElt, fraction);
    }

    @Override
    public DirVector getDerivative() {
        DirVector dx = activeElement.to(nextElement(), new DirVector());
        float dy = getTimeDifference();
        return dx.scale(1 / dy);
    }

}
