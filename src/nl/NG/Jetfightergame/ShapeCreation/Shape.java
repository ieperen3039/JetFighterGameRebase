package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Primitives.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.Renderable;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
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

    /** @see #getPlanes() */
    default Stream<? extends Plane> getPlaneStream() {
        return StreamSupport.stream(getPlanes().spliterator(), false);
    }

    /** @see #getPoints() */
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

    default Pair<PosVector, Float> getMinimalCircle() {
        return getMinimalCircle(getPoints());
    }

    /**
     * Calculates the smallest orb around the given points
     * @param points a number of points, at least two
     * @return Left, the middle of the found orb. Right, the radius of the given orb
     */
    static Pair<PosVector, Float> getMinimalCircle(Iterable<PosVector> points) {
        // determine furthest two point
        float duoMax = 0;
        DirVector temp = new DirVector();
        PosVector aMax = new PosVector();
        PosVector bMax = new PosVector();
        for (PosVector a : points) {
            for (PosVector b : points) {
                float dist = a.to(b, temp).lengthSquared();
                if (dist > duoMax) {
                    duoMax = dist;
                    aMax.set(a);
                    bMax.set(b);
                }
            }
        }

        // determine point furthest from the middle
        PosVector mid = aMax.middleTo(bMax);
        PosVector outer = new PosVector();
        float tripleMax = 0;
        for (PosVector vector : points) {
            float dist = mid.to(vector, temp).lengthSquared();
            if (dist > tripleMax) {
                outer.set(vector);
                tripleMax = dist;
            }
        }

        // if this point is none of the two previous points, determine the circumscribed circle
        // https://en.wikipedia.org/wiki/Circumscribed_circle
        if ((tripleMax > (duoMax / 4)) && !(outer.equals(aMax) || outer.equals(bMax))) {
            PosVector temp2 = new PosVector();
            PosVector temp3 = new PosVector();

            PosVector a = aMax.sub(outer, new PosVector());
            PosVector b = bMax.sub(outer, new PosVector());

            PosVector dif = b.scale(a.lengthSquared(), temp2)
                    .sub(a.scale(b.lengthSquared(), temp3), temp2);
            float scalar = 2 * a.cross(b, temp3).lengthSquared();

            mid.set(
                    dif.cross(a.cross(b, temp)).div(scalar).add(outer)
            );
        }

        return new Pair<>(mid, mid.to(aMax, temp).length());
    }
}
