package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen created on 10-4-2018.
 */
public interface PathDescription {

    /**
     * calculates a position that is on 1 accuracy the closest to the nearest middle of the path.
     * @param position the position of some entity
     * @return the nearest middle of the path.
     */
    PosVector getMiddleOfPath(PosVector position);

}
