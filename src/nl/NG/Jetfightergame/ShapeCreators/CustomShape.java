package nl.NG.Jetfightergame.ShapeCreators;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedObject;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.io.IOException;
import java.io.PrintWriter;
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
        faces = new ArrayList<>();
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
    private static Vector bezierPoint(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        //A*(1−u)^3 + B*3u(1−u)^2 + C*3u^2(1−u) + D*u^3
        return A.scale((float) ((1 - u) * (1 - u) * (1 - u)))
                .add(B.scale((float) (3 * u * (1 - u) * (1 - u))))
                .add(C.scale((float) (3 * u * u * (1 - u))))
                .add(D.scale((float) (u * u * u)));
    }

    /**
     * evaluates the derivative of a beziér curve on a point defined by u
     *
     * @see CustomShape#bezierPoint(PosVector, PosVector, PosVector, PosVector, double)
     */
    private static DirVector bezierDerivative(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        //(B-A)*3*(1-u)^2 + (C-B)*6*(1-u)*u + (D-C)*3*u^2
        return (B.subtract(A)).scale((float) (3 * (1 - u) * (1 - u)))
                .add(C.subtract(B).scale((float) (6 * (1 - u) * u)))
                .add(D.subtract(C).scale((float) (3 * u * u)))
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
        addTriangle(A, D, C, normal);
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
        if (normal == null) throw new IllegalArgumentException("Customshape.addNormal(DirVector): normal can not be null");
        DirVector normalizedNormal = normal.normalized();
        normals.putIfAbsent(normalizedNormal, normals.size());
        return normals.get(normalizedNormal);
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
    public Pair<List<PosVector>, List<PosVector>> addBezierStrip(PosVector A1, PosVector A2, PosVector B1, PosVector B2,
                                                                 PosVector C1, PosVector C2, PosVector D1, PosVector D2,
                                                                 double slices) {

        DirVector startNormal = bezierDerivative(A2, B2, C2, D2, 0);
        if (startNormal.dot(A2.to(middle)) > 0) startNormal = startNormal.scale(-1);
        TrackedObject<DirVector> normal = new TrackedVector<>(startNormal);

        List<PosVector> leftVertices = new ArrayList<>();
        List<PosVector> rightVertices = new ArrayList<>();

        // initialize the considered vertices by their starting point
        TrackedObject<PosVector> left = new TrackedObject<>(bezierPoint(A1, B1, C1, D1, 0).toPosVector());
        TrackedObject<PosVector> right = new TrackedObject<>(bezierPoint(A2, B2, C2, D2, 0).toPosVector());

        // add vertices for every part of the slice, and combine these into a quad
        for (int i = 1; i <= slices; i++) {
            left.update(bezierPoint(A1, B1, C1, D1, i / slices).toPosVector());
            leftVertices.add(left.current());

            right.update(bezierPoint(A2, B2, C2, D2, i / slices).toPosVector());
            rightVertices.add(right.current());

            DirVector newNormal = bezierDerivative(A2, B2, C2, D2, i / slices);
            newNormal = newNormal.dot(normal.previous()) > 0 ? newNormal : newNormal.scale(-1);
            normal.update(newNormal);

            addQuad(left.previous(), right.previous(), right.current(), left.current(), normal.current());
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
     * @return this strip defined as two lists of points each defining one side of the strip
     * @see CustomShape#addBezierStrip
     */
    public Pair<List<PosVector>, List<PosVector>> addBezierStrip(PosVector start, PosVector M, PosVector end, int slices) {
        PosVector B = start.middleTo(M);
        PosVector C = end.middleTo(M);
        return addBezierStrip(start, start.mirrorY(), B, B.mirrorY(), C, C.mirrorY(), end, end.mirrorY(), slices);
    }

    /**
     * creates a plane connecting an existing beziér curve to a point
     *
     * @param point      the point where the curve must be connected to
     * @param strip      the id returned upon creation of the specific curve
     * @param takeLeft   {@code true} if the first vectors should be accounted
     *                   {@code false} if the second vectors should be accounted
     */
    public void addPlaneToBezierStrip(PosVector point, Pair<List<PosVector>, List<PosVector>> strip, boolean takeLeft) {
        Iterator<PosVector> positions = (takeLeft ? strip.left : strip.right).iterator();

        TrackedObject<PosVector> targets = new TrackedVector<>(positions.next());
        while (positions.hasNext()) {
            targets.update(positions.next());
            DirVector normal = Plane.getNormalVector(point, targets.previous(), targets.current(), middle);
            addTriangle(targets.previous(), targets.current(), point, normal);
        }
    }

    /**
     * adds a strip as separate quad objects
     * @param quads an array of 2n+4 vertices defining quads as
     * {@link #addQuad(PosVector, PosVector, PosVector, PosVector)} for every natural number n.
     */
    public void addStrip(PosVector ... quads) {
        final int inputSize = quads.length;
        if (inputSize % 2 != 0 || inputSize < 4){
            throw new IllegalArgumentException(
                    "input arguments can not be of odd length or less than 4 (length is "+ inputSize + ")");
        }

        for (int i = 4; i < inputSize; i += 2) {
            // create quad as [1, 2, 4, 3], as rotational order is required
            addQuad(quads[i - 4], quads[i - 3], quads[i - 1], quads[i - 2]);
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
        List<PosVector> vertexList = new ArrayList<>();
        Collections.addAll(vertexList, sortedVertices);
        List<DirVector> normalList = new ArrayList<>();
        Collections.addAll(normalList, sortedNormals);

        return new ShapeFromMesh(vertexList, normalList, faces);
    }

    /**
     * writes an object to the given filename
     * @param filename a name of a (preferably non-existing) file without extension
     * @throws IOException if any problem occurs while creating the file
     */
    public void writeOBJFile(String filename) throws IOException {
        PrintWriter writer = new PrintWriter(filename + ".obj", "UTF-8");

        writer.println("# created using a simple obj writer by Geert van Ieperen");
        writer.println("# calling method: " + Toolbox.getCallingMethod(2));
        writer.println("mtllib arrow.mtl");

        PosVector[] sortedVertices = new PosVector[points.size()];
        points.forEach((v, i) -> sortedVertices[i] = v);
        DirVector[] sortedNormals = new DirVector[normals.size()];
        normals.forEach((v, i) -> sortedNormals[i] = v);

        for (PosVector vec : sortedVertices) {
            writer.println(String.format(Locale.US,"v %1.09f %1.09f %1.09f", vec.x(), vec.z(), vec.y()));
        }

        for (DirVector vec : sortedNormals) {
            writer.println(String.format(Locale.US,"vn %1.09f %1.09f %1.09f", vec.x(), vec.z(), vec.y()));
        }

        writer.println("usemtl None");
        writer.println("s off");
        writer.println("");

        for (Mesh.Face face : faces) {
            writer.println("f " + readVertex(face.A) + " " + readVertex(face.B) + " " + readVertex(face.C));
        }

        writer.close();

        Toolbox.print("Successfully created obj file: " + writer.toString());
    }

    private static String readVertex(Pair<Integer, Integer> vertex) {
        return String.format("%d//%d", vertex.left + 1, vertex.right + 1);
    }
}
