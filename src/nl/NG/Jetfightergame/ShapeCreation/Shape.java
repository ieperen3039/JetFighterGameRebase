package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.Renderable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geert van Ieperen
 *         created on 30-10-2017.
 */
public interface Shape extends Renderable {

    /** returns all planes of this object in no specific order */
    Iterable<? extends Plane> getPlanes();

    /** @return the points of this plane in no specific order */
    Iterable<PosVector> getPoints();

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
    default Collision getCollision(PosVector linePosition, DirVector direction, PosVector endPoint){
        return getPlaneStream()
                // find the vector that hits the planes
                .map((plane) -> plane.getCollisionWith(linePosition, direction, endPoint))
                // exclude the vectors that did not hit
                .filter(Objects::nonNull)
                // return the shortest vector
                .min(Collision::compareTo)
                .orElse(null);
    }

    default Stream<? extends Plane> getPlaneStream() {
        return StreamSupport.stream(getPlanes().spliterator(), false);
    }

    default Stream<? extends PosVector> getPointStream() {
        return StreamSupport.stream(getPoints().spliterator(), false);
    }

    /**
     * given a ray, determines if this ray hit this shape. Note that this method assumes local vectors,
     * thus the parameters must be transformed to local space using for instance {@link ShadowMatrix#mapToLocal(PosVector)}
     * @param position the local begin point of the ray
     * @param direction the local direction in which the ray progresses. Does not have to be normalized
     * @return true iff this shape intersects this ray
     */
    default boolean isHitByRay(PosVector position, DirVector direction){
        return getPlaneStream()
                // find the vector that hits the planes
                .map((plane) -> plane.intersectWithRay(position, direction))
                // return whether at least one hit
                .findAny().orElse(false);
    }
}
