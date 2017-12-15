package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class VectorInterpolator extends Interpolator<Vector> {

    public VectorInterpolator(int capacity) {
        super(capacity);
    }

    public Vector Interpolate(float timeStamp, double firstTime, Vector firstElt, double secondTime, Vector secondElt) {
        double fraction = (timeStamp - firstTime) / (secondTime - firstTime);
        DirVector difference = firstElt.to(secondElt).toDirVector();

        Toolbox.print(timeUntilNext(timeStamp), fraction);

        return firstElt.add(difference.scale(fraction));
    }
}
