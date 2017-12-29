package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *
 * a plane with hitbox, allowing checks whether a line-piece hits the plane.
 * all vectors are in a given frame of reference
 */
public abstract class Plane {
    /**
     * a summation of references to define the bounding box of this plane.
     * often, these refer to the same vectors
     */
    protected float leastX = Float.MAX_VALUE;
    protected float mostX = -Float.MAX_VALUE;
    protected float leastY = Float.MAX_VALUE;
    protected float mostY = -Float.MAX_VALUE;
    protected float leastZ = Float.MAX_VALUE;
    protected float mostZ = -Float.MAX_VALUE;

    /** not necessarily normalized */
    protected final DirVector normal;

    protected final PosVector[] boundary;

    /** reserved space for collision detection */
    private PosVector relativePosition = new PosVector();

    public Plane(PosVector[] vertices, DirVector normal) {
        this.normal = normal;
        this.boundary = vertices;

        // initialize hitbox
        for (PosVector posVector : vertices) {
            if (posVector.x < leastX) leastX = posVector.x;
            if (posVector.x > mostX) mostX = posVector.x;
            if (posVector.y < leastY) leastY = posVector.y;
            if (posVector.y > mostY) mostY = posVector.y;
            if (posVector.z < leastZ) leastZ = posVector.z;
            if (posVector.z > mostZ) mostZ = posVector.z;
        }
    }

    public static DirVector getNormalVector(PosVector A, PosVector B, PosVector C) {
        DirVector normalVector = new DirVector();
        final DirVector BC = new DirVector();
        final DirVector BA = new DirVector();

        B.to(C, BC);
        B.to(A, BA);
        BA.cross(BC, normalVector);

        normalVector.normalize();

        return normalVector;
    }


    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction},
     * calculates the movement it is allowed to do before hitting this plane
     * (template design)
     *
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1
     */
    public Collision getHitvector(PosVector linePosition, DirVector direction, PosVector endPoint) {
        if (hasWrongDirection(direction)) return null;

        if (asideHitbox(linePosition, endPoint)) return null;

        DirVector hitDir = calculateMaxDirection(linePosition, direction);

        if (hitDir.length() > direction.length()) return null;

        final PosVector hitPos = linePosition.add(hitDir, relativePosition);
        if (!this.encapsules(hitPos)) return null;

        return new Collision(hitDir.length() / direction.length(), normal, hitPos);
    }

    /**
     * @param direction the direction of a given linepiece
     * @return false if the direction opposes the normal-vector, eg if it could hit the plane
     */
    protected boolean hasWrongDirection(DirVector direction) {
        return direction.dot(normal) >= 0;
    }

    /**
     * returns whether two points are completely on one side of the plane
     * this standard implementation uses the predefined hitbox, may be overridden for efficiency
     *
     * @param alpha a point in local space
     * @param beta  another point in local space
     * @return true if both points exceed the extremest vector in either x, y, z, -x, -y or -z direction.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    protected boolean asideHitbox(PosVector alpha, PosVector beta) {
        if ((alpha.x() < leastX) && (beta.x() < leastX) || (alpha.x() > mostX) && (beta.x() > mostX))
            return true;
        if ((alpha.y() < leastY) && (beta.y() < leastY) || (alpha.y() > mostY) && (beta.y() > mostY))
            return true;
        return (alpha.z() < leastZ) && (beta.z() < leastZ) || (alpha.z() > mostZ) && (beta.z() > mostZ);
    }

    /**
     * determines whether the given point lies on or within the boundary, given that it lies on
     * the infinite extension of this plane
     *
     * @param hitPos a point on this plane
     * @return true if the point is not outside the boundary of this plane
     * @precondition hitPos lies on the plane of the points of {@code boundary}
     */
    protected abstract boolean encapsules(PosVector hitPos);

    /**
     * calculates the new {@param direction}, relative to {@param linePosition}
     * where the given line will hit this plane if this plane was infinite
     *
     * @return Vector D such that (linePosition.add(D)) will give the position of the hitPoint.
     * D lies in the extend, but not necessarily on the plane
     */
    protected DirVector calculateMaxDirection(PosVector linePosition, DirVector direction) {
        // random point is chosen
        boundary[0].sub(linePosition, relativePosition);

        float scalar = (relativePosition.dot(normal) / direction.dot(normal));

        if (Vector.almostZero(scalar)) {
            return DirVector.zeroVector();
        } else {
            return direction.scale(scalar, new DirVector());
        }
    }

    public Collection<PosVector> getVertices() {
        return Arrays.asList(boundary);
    }

    public Stream<PosVector> getBorderAsStream() {
        return Arrays.stream(boundary);
    }

    public DirVector getNormal() {
        return normal;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(this.getClass().getSimpleName());
        s.append("{");
        for (PosVector posVector : boundary) {
            s.append(posVector);
        }
        s.append("}");
        return s.toString();
    }

    /**
     * given a ray, determines whether it intersects this plane.
     * @param position the starting point of the line
     * @param direction the direction vector of the line
     * @return true if this plane intersects with the line extended toward infinity
     */
    public boolean intersectWithRay(PosVector position, DirVector direction){
        DirVector hitDir = calculateMaxDirection(position, direction);
        if (hitDir.dot(direction) < 0) return false;

        PosVector hitPoint = position.add(hitDir, new PosVector());
        return this.encapsules(hitPoint);
    }
}

