package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.GameObjects.Surfaces.Plane;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedObject;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.util.*;

/**
 * Created by Geert van Ieperen on 1-3-2017.
 * defines a custom, static object shape
 */
public class CustomShape {

    private final PosVector middle;
    private final Map<PosVector, Integer> points;
    private final Map<DirVector, Integer> normals;
    private final List<Mesh.Face> faces;

    public CustomShape() {
        this(PosVector.O);
    }

    public CustomShape(PosVector middle) {
        this.middle = middle;
        faces = new LinkedList<>();
        points = new Hashtable<>();
        normals = new Hashtable<>();
    }

    /**
     * evaluates a beziér curve defined by vectors
     *
     * @param A starting point
     * @param B first control point
     * @param C second control point
     * @param D ending point
     * @param u fraction of the curve to be requested
     * @return vector to the point on the curve on fraction u
     */
    private static Vector beziérPoint(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        //A*(1−u)^3 + B*3u(1−u)^2 + C*3u^2(1−u) + D*u^3
        return A.scale((1 - u) * (1 - u) * (1 - u))
                .add(B.scale(3 * u * (1 - u) * (1 - u)))
                .add(C.scale(3 * u * u * (1 - u)))
                .add(D.scale(u * u * u));
    }

    /**
     * evaluates the derivative of a beziér curve on a point defined by u
     *
     * @see CustomShape#beziérPoint(PosVector, PosVector, PosVector, PosVector, double)
     */
    private static DirVector beziérDerivative(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        //(B-A)*3*(1-u)^2 + (C-B)*6*(1-u)*u + (D-C)*3*u^2
        return (B.subtract(A))
                .scale(3 * (1 - u) * (1 - u))
                .add(C.subtract(B).scale(6 * (1 - u) * u))
                .add(D.subtract(C).scale(3 * u * u))
                .toDirVector();
    }

    /**
     * defines a quad in rotational order
     *
     * @param A      (0, 0)
     * @param B      (0, 1)
     * @param C      (1, 1)
     * @param D      (1, 0)
     * @param normal the normal of this plane
     * @throws NullPointerException if any of the vectors is null
     */
    public void addQuad(PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal) {
        addTriangle(A, B, C, normal);
        addTriangle(B, C, D, normal);
    }

    /**
     * defines a quad that is mirrored over the xz-plane
     *
     * @see CustomShape#addQuad(PosVector, PosVector, PosVector, PosVector, DirVector)
     */
    public void addQuad(PosVector A, PosVector B) {
        addQuad(A, B, B.mirrorY(), A.mirrorY());
    }

    /**
     * @see CustomShape#addQuad(PosVector, PosVector, PosVector, PosVector, DirVector)
     */
    public void addQuad(PosVector A, PosVector B, PosVector C, PosVector D) {
        addQuad(A, B, C, D, Plane.getNormalVector(A, B, C, middle));
    }

    /**
     * Adds a quad which is mirrored in the XZ-plane
     */
    public void addMirrorQuad(PosVector A, PosVector B, PosVector C, PosVector D) {
        DirVector quadNormal = Plane.getNormalVector(A, B, C, middle);
        addQuad(A, B, C, D, quadNormal);
        addQuad(A.mirrorY(), B.mirrorY(), C.mirrorY(), D.mirrorY(), quadNormal.scale(-1));
    }

    /**
     * @see CustomShape#addTriangle(PosVector, PosVector, PosVector, DirVector)
     */
    public void addTriangle(PosVector A, PosVector B, PosVector C) {
        addTriangle(A, B, C, Plane.getNormalVector(A, B, C, middle));
    }

    /**
     * defines a triangle in the three given points
     *
     * @see CustomShape#addQuad(PosVector, PosVector, PosVector, PosVector)
     */
    public void addTriangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        int aInd = addHitpoint(A);
        int bInd = addHitpoint(B);
        int cInd = addHitpoint(C);
        int nInd = addNormal(normal);
        faces.add(new Mesh.Face(aInd, bInd, cInd, nInd));
    }

    private int addNormal(DirVector normal) {
        normals.putIfAbsent(normal, normals.size());
        return points.get(normal);
    }

    /**
     * stores a vector in the collection, and returns its resulting position
     * @param vector
     * @return index of the vector
     */
    private int addHitpoint(PosVector vector) {
        points.putIfAbsent(vector, points.size());
        return points.get(vector);
    }

    /**
     * Adds a triangle which is mirrored in the XZ-plane
     */
    public void addMirrorTriangle(PosVector A, PosVector B, PosVector C) {
        addTriangle(A, B, C);
        addTriangle(A.mirrorY(), B.mirrorY(), C.mirrorY());
    }

    /**
     * Adds a strip defined by a beziér curve
     * the 1-vectors are the curve of one size, the 2-vectors are the curve of the other side
     *
     * @param slices number of fractions of the curve
     * @return either side of the strip, with left the row starting with A1
     */
    public Pair<List<PosVector>, List<PosVector>> addBeziérStrip(PosVector A1, PosVector A2, PosVector B1, PosVector B2,
                                                                 PosVector C1, PosVector C2, PosVector D1, PosVector D2,
                                                                 double slices) {

        DirVector rightNormal = beziérDerivative(A2, B2, C2, D2, 0);
        if (rightNormal.dot(A2.to(middle)) > 0) rightNormal = rightNormal.scale(-1);

        // the 'running' normal
        DirVector normal;

        List<PosVector> leftVertices = new LinkedList<>();
        List<PosVector> rightVertices = new LinkedList<>();

        TrackedObject<PosVector> left = new TrackedObject<>(PosVector.O);
        TrackedObject<PosVector> right = new TrackedObject<>(PosVector.O);

        for (int i = 0; i <= slices; i++) {
            left.update(beziérPoint(A1, B1, C1, D1, i / slices).toPosVector());
            leftVertices.add(left.current());

            right.update(beziérPoint(A2, B2, C2, D2, i / slices).toPosVector());
            normal = beziérDerivative(A2, B2, C2, D2, i / slices);
            rightNormal = (rightNormal.dot(normal) > 0 ? normal : normal.scale(-1));
            rightVertices.add(right.current());

            if (i > 1){
                addQuad(left.previous(), right.previous(), right.current(), left.current(), normal);
            }
        }

        return new Pair<>(leftVertices, rightVertices);
    }

    /**
     * adds a simple beziér curve, mirrored over the xz plane
     *
     * @param start the starting point of the curve
     * @param M     a point indicating the direction of the curve
     *              (NOT the middle control point, but the direction coefficients DO point to M)
     * @param end   the endpoint of the curve
     * @returns pointer to this strip
     * @see CustomShape#addBeziérStrip
     */
    public Pair<List<PosVector>, List<PosVector>> addBeziérStrip(PosVector start, PosVector M, PosVector end, int slices) {
        PosVector B = start.middleTo(M);
        PosVector C = end.middleTo(M);
        return addBeziérStrip(start, start.mirrorY(), B, B.mirrorY(), C, C.mirrorY(), end, end.mirrorY(), slices);
    }

    /**
     * creates a plane connecting an existing beziér curve to a point
     *
     * @param point      the point where the curve must be connected to
     * @param strip      the id returned upon creation of the specific curve
     * @param takeLeft   {@code true} if the first vectors should be accounted
     *                   {@code false} if the second vectors should be accounted
     */
    public void addPlaneToBeziérStrip(PosVector point, Pair<List<PosVector>, List<PosVector>> strip, boolean takeLeft) {

        List<PosVector> curve = (takeLeft ? strip.left : strip.right);

        assert curve != null;
        for (int i = 0; i < curve.size() - 1; i++) {
            PosVector alpha = curve.get(i);
            PosVector beta = curve.get(i);
            DirVector normal = Plane.getNormalVector(point, alpha, beta, middle);
            addTriangle(alpha, beta, point, normal);
        }
    }

    /**
     * convert this object to a shape
     * @return a shape with hardware-accelerated graphics using the {@link ShapeFromMesh} object
     */
    public Shape wrapUp(){
        PosVector[] sortedVertices = new PosVector[points.size()];
        points.forEach((v, i) -> sortedVertices[i] = v);
        DirVector[] sortedNormals = new DirVector[normals.size()];
        normals.forEach((v, i) -> sortedNormals[i] = v);

        // this is the most clear, structured way, maybe not the most efficient (yet this is initialization)
        List<PosVector> vertexList = new LinkedList<>();
        List<DirVector> normalList = new LinkedList<>();
        Collections.addAll(vertexList, sortedVertices);
        Collections.addAll(normalList, sortedNormals);

        return new ShapeFromMesh(vertexList, normalList, faces);
    }
}
