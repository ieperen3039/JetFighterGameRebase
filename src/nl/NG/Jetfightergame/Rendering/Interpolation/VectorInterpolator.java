package nl.NG.Jetfightergame.Rendering.Interpolation;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class VectorInterpolator extends LinearInterpolator<Vector> {

    public VectorInterpolator(int capacity, Vector initialValue) {
        super(capacity, initialValue);
    }

    protected Vector interpolate(Vector firstElt, Vector secondElt, float fraction) {
        DirVector difference = firstElt.to(secondElt, new DirVector());

        Vector dest = new PosVector();
        firstElt.add(difference.scale(fraction, difference), dest);
        
        if (!dest.isScalable()) dest.set(firstElt);
        return dest;
    }
}
