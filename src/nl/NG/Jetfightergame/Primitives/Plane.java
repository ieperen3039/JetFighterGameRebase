package nl.NG.Jetfightergame.Primitives;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

import java.util.Arrays;

/**
 * @author Geert van Ieperen
 *         <p>
 *         a plane with hitbox, allowing checks whether a line-piece hits the plane. all vectors are in a given frame of
 *         reference
 */
public abstract class Plane {
    /**
     * a summation of references to define the bounding box of this plane. often, these refer to the same vectors
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
    private PosVector middle = null;

    /**
     * @param vertices the vertices in counterclockwise order
     * @param normal   the normal vector in opposite direction of how it is visible
     */
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

    /**
     * given a point on position {@code linePosition} moving in the direction of {@code direction}, calculates the
     * movement it is allowed to do before hitting this plane (template design)
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)} if this vector is
     *                     null, an infinite line is assumed
     * @return {@code null} if it does not hit with direction scalar < 1
     */
    public Collision getCollisionWith(PosVector linePosition, DirVector direction, PosVector endPoint) {
        final boolean isInfinite = (endPoint == null);

        if (hasWrongDirection(direction)) return null;

        if (!isInfinite && asideHitbox(linePosition, endPoint)) return null;

        float scalar = hitScalar(linePosition, direction);
        if (!isInfinite && (scalar > 1.0f)) return null;

        DirVector hitDir = direction.scale(scalar, new DirVector());
        PosVector hitPos = linePosition.add(hitDir, new PosVector());
        if (!this.encapsulates(hitPos)) return null;

        return new Collision(scalar, normal, hitPos);
    }

    /**
     * @param direction the direction of a given linepiece
     * @return false if the direction opposes the normal-vector, eg if it could hit the plane
     */
    protected boolean hasWrongDirection(DirVector direction) {
        return direction.dot(normal) >= 0;
    }

    /**
     * returns whether two points are completely on one side of the plane this standard implementation uses the
     * predefined hitbox, may be overridden for efficiency
     * @param alpha a point in local space
     * @param beta  another point in local space
     * @return true if both points exceed the extremest vector in either x, y, z, -x, -y or -z direction.
     */
    @SuppressWarnings("SimplifiableIfStatement")
    protected boolean asideHitbox(PosVector alpha, PosVector beta) {
        if (((alpha.x() < leastX) && (beta.x() < leastX)) || ((alpha.x() > mostX) && (beta.x() > mostX)))
            return true;
        if (((alpha.y() < leastY) && (beta.y() < leastY)) || ((alpha.y() > mostY) && (beta.y() > mostY)))
            return true;
        return ((alpha.z() < leastZ) && (beta.z() < leastZ)) || ((alpha.z() > mostZ) && (beta.z() > mostZ));
    }

    /**
     * determines whether the given point lies on or within the boundary, given that it lies on the infinite extension
     * of this plane
     * @param hitPos a point on this plane
     * @return true if the point is not outside the boundary of this plane
     * @precondition hitPos lies on the plane of the points of {@code boundary}
     */
    protected abstract boolean encapsulates(PosVector hitPos);

    /**
     * calculates the new {@param direction}, relative to {@param linePosition} where the given line will hit this plane
     * if this plane was infinite
     * @return Vector D such that (linePosition.add(D)) will give the position of the hitPoint. D lies in the extend,
     *         but not necessarily on the plane. D is given by ((p0 - l0)*n) \ (l*n)
     */
    protected float hitScalar(PosVector linePosition, DirVector direction) {
        relativePosition.set(boundary[0]);

        float upper = relativePosition.sub(linePosition, relativePosition).dot(normal);
        float lower = direction.dot(normal);
        return upper / lower;
    }

    /**
     * @return a stream of the vertices of this object in counterclockwise order
     */
    public Iterable<PosVector> getBorder() {
        return Arrays.asList(boundary);
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
     * @param position  the starting point of the line
     * @param direction the direction vector of the line
     * @return true if this plane intersects with the line extended toward infinity
     */
    public boolean intersectWithRay(PosVector position, DirVector direction) {
        DirVector hitDir = direction.scale(hitScalar(position, direction), new DirVector());

        if (hitDir.dot(direction) < 0) return false;

        PosVector hitPoint = position.add(hitDir, new PosVector());
        return this.encapsulates(hitPoint);
    }

    /**
     * @return the local average of all border positions
     */
    public PosVector getMiddle() {
        if (middle == null) {
            middle = PosVector.zeroVector();
            int i = 0;
            while (i < boundary.length) {
                middle.add(boundary[i]);
                i++;
            }
            middle.div(i);
        }
        return middle;
    }

    /**
     * calculates the normal vector for a triangle given in counterclockwise order
     * @return the normalized normal for a triangle ABC
     */
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
     * evaluates a beziér curve defined by vectors. The given ABCD vectors are kept intact.
     * @param A starting point
     * @param B first control point
     * @param C second control point
     * @param D ending point
     * @param u fraction of the curve to be requested
     * @return vector to the point on the curve on fraction u
     */
    public static Vector bezierPoint(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        PosVector temp = new PosVector();
        PosVector point = new PosVector();
        //A*(1−u)^3 + B*3u(1−u)^2 + C*3u^2(1−u) + D*u^3
        A.scale((float) ((1 - u) * (1 - u) * (1 - u)), point)
                .add(B.scale((float) (3 * u * (1 - u) * (1 - u)), temp), point)
                .add(C.scale((float) (3 * u * u * (1 - u)), temp), point)
                .add(D.scale((float) (u * u * u), temp), point);
        return point;
    }

    /**
     * evaluates the derivative of a beziér curve on a point defined by u
     * @see #bezierPoint(PosVector, PosVector, PosVector, PosVector, double)
     */
    public static DirVector bezierDerivative(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        DirVector direction = new DirVector();
        PosVector temp = new PosVector();
        final PosVector point = new PosVector();
        //(B-A)*3*(1-u)^2 + (C-B)*6*(1-u)*u + (D-C)*3*u^2
        (B.sub(A, point))
                .scale((float) (3 * (1 - u) * (1 - u)), point)
                .add(C.sub(B, temp).scale((float) (6 * (1 - u) * u), temp), direction)
                .add(D.sub(C, temp).scale((float) (3 * u * u), temp), direction);
        return direction;
    }

}

