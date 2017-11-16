package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 13-11-2017.
 * Does not consider minimal view range.
 */
public class ViewFrustum {
    /***/
    public final float viewRange;
    /***/
    public final DirVector normViewDirection;
    /***/
    public final PosVector eyePosition;

    public ViewFrustum(float fovHorizontal, float fovVertical, float viewRange, PosVector eyePosition, DirVector viewDirection, DirVector up) {
        this.viewRange = viewRange;
        this.normViewDirection = viewDirection.normalized();
        this.eyePosition = eyePosition;

    }

    /**
     * checks the frustum against a posvector to quickly determine whether it is in view
     * @param position position of an object
     * @param radius maximum radius around the object
     * @return false if the object is guaranteed not in view. true if it might be
     */
    public boolean isInView(PosVector position, float radius) {
        final PosVector relativePosition = position.subtract(eyePosition);
        final double distance = (relativePosition.dot(normViewDirection));

        // true unless too far away or strictly behind
        return (distance < viewRange && distance + radius > 0);
    }
}
