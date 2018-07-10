package nl.NG.Jetfightergame.Tools.Interpolation;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class VectorInterpolator extends LinearInterpolator<Vector> {

    public VectorInterpolator(int capacity, Vector initialVector, float initialTime) {
        super(capacity, initialVector, initialTime);
    }

    /**
     * creates an interpolator with two values already set. Make sure that firstTime < secondTime
     */
    public VectorInterpolator(int capacity, Vector firstElement, float firstTime, Vector secondElement, float secondTime) {
        super(capacity, firstElement, firstTime, secondElement, secondTime);
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
