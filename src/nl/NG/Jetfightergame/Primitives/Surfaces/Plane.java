package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.GameObjects.Hitbox.Collision;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.util.List;

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
    protected final double leastX;
    protected final double mostX;
    protected final double leastY;
    protected final double mostY;
    protected final double leastZ;
    protected final double mostZ;
    /** not necessarily normalized */
    protected final DirVector normal;

    protected final List<PosVector> boundary;

    public Plane(DirVector normal, List<PosVector> vertices) {
        this.normal = normal;
        this.boundary = vertices;

        // initialize hitbox
        leastX = boundary.stream().mapToDouble(Vector::x).min().orElse(0);
        mostX = boundary.stream().mapToDouble(Vector::x).max().orElse(0);
        leastY = boundary.stream().mapToDouble(Vector::y).min().orElse(0);
        mostY = boundary.stream().mapToDouble(Vector::y).max().orElse(0);
        leastZ = boundary.stream().mapToDouble(Vector::z).min().orElse(0);
        mostZ = boundary.stream().mapToDouble(Vector::z).max().orElse(0);
    }

    /**
     * @param middle a point that is assumed to be on the inside side of the plane
     * @return normal vector that points outward the xz-plane, or when orthogonal away from 'middle'
     */
    public static DirVector getNormalVector(PosVector A, PosVector B, PosVector C, PosVector middle) {
        DirVector normalVector = B.to(A).cross(B.to(C));
        if (normalVector.dot(B.to(middle)) > 0) {
            normalVector = normalVector.scale(-1);
        }
        return normalVector;
    }

    /**
     * @param direction a point for which hold normal.dot(direction) > 0
     * @return normal vector that points outward the xz-plane, or when orthogonal away from 'middle'
     */
    public static DirVector getNormalVector(PosVector A, PosVector B, PosVector C, DirVector direction){
        DirVector normalVector = B.to(A).cross(B.to(C));
        if (normalVector.dot(direction) < 0) normalVector = normalVector.scale(-1);
        return normalVector.normalized();
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

        if (!isWithin(linePosition.add(hitDir))) return null;

        return new Collision(hitDir.length() / direction.length(), normal);
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
     * determines whether the given point lies on or within the boundary given a point that lies on
     * the infinite extension of this plane
     *
     * @param hitPos a point on this plane
     * @return true if the point is not outside the boundary of this plane
     * @pre hitPos lies on the plane between the points in {@code boundary}
     */
    protected abstract boolean isWithin(PosVector hitPos);

    /**
     * calculates the new {@param direction}, relative to {@param linePosition}
     * where the given line will hit this plane if this plane was infinite
     *
     * @return Vector D such that (linePosition.add(D)) will give the position of the hitPoint.
     * Lies in the extend, but not necessarily on the plane
     */
    protected DirVector calculateMaxDirection(PosVector linePosition, DirVector direction) {
        // random point is taken
        PosVector offSquare = boundary.get(0).subtract(linePosition);
        double scalar = (offSquare.dot(normal) / direction.dot(normal));

        if (Vector.almostZero(scalar)) {
            return DirVector.O;
        } else {
            return direction.scale(scalar);
        }
    }

    public List<PosVector> getVertices() {
        return boundary;
    }

    public DirVector getNormal() {
        return normal;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(this.getClass().getSimpleName());
        s.append("{");
        boundary.forEach(s::append);
        s.append("}");
        return s.toString();
    }
}

