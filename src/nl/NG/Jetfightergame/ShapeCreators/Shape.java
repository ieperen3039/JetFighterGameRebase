package nl.NG.Jetfightergame.ShapeCreators;

import nl.NG.Jetfightergame.Engine.GLMatrix.Renderable;
import nl.NG.Jetfightergame.EntityDefinitions.Hitbox.Collision;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface Shape extends Renderable {

    /** returns all planes of this object */
    Stream<? extends Plane> getPlanes();

    Collection<PosVector> getPoints();

    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction},
     * calculates the movement it is allowed to do before hitting this shape
     *
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1
     * otherwise, it provides a collision object about the first collision with this shape
     */
    default Collision getMaximumMovement(PosVector linePosition, DirVector direction, PosVector endPoint){
        return getPlanes()
                // find the vector that hits the planes
                .map((plane) -> plane.getHitvector(linePosition, direction, endPoint))
                // exclude the vectors that did not hit
                .filter(Objects::nonNull)
                // return the shortest vector
                .min(Collision::compareTo)
                .orElse(null);
    }
}
