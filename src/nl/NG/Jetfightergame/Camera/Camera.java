package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public interface Camera {

    DirVector vectorToFocus();

    void updatePosition(TrackedFloat timer);

    PosVector getEye();

    PosVector getFocus();

    DirVector getUpVector();
}
