package nl.NG.Jetfightergame.ShapeCreators;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
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
    private final List<DirVector> normals;
    private final List<Mesh.Face> faces;

    public CustomShape() {
        this(PosVector.zeroVector());
    }

    /**
     * a shape that may be defined by the client code using methods of this class.
     * When the shape is finished, call {@link #wrapUp()} to load it into the GPU.
     * The returned shape should be re-used as a static mesh for any future calls to such shape.
     * @param middle the middle of this object. More specifically, from this point,
     *               all normal vectors point outward except maybe for those that have their normal explicitly defined.
     */
    public CustomShape(PosVector middle) {
        this.middle = middle;
        faces = new ArrayList<>();
        points = new Hashtable<>();
        normals = new ArrayList<>();
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
        PosVector point = new PosVector();
        //A*(1−u)^3 + B*3u(1−u)^2 + C*3u^2(1−u) + D*u^3
        A.scale((float) ((1 - u) * (1 - u) * (1 - u)), point)
                .add(B.scale((float) (3 * u * (1 - u) * (1 - u)), new PosVector()), point)
                .add(C.scale((float) (3 * u * u * (1 - u)), new PosVector()), point)
                .add(D.scale((float) (u * u * u), new PosVector()), point);
        return point;
    }

    /**
     * evaluates the derivative of a beziér curve on a point defined by u
     *
     * @see CustomShape#bezierPoint(PosVector, PosVector, PosVector, PosVector, double)
     */
    private static DirVector bezierDerivative(PosVector A, PosVector B, PosVector C, PosVector D, double u) {
        DirVector direction = new DirVector();
        final PosVector point = new PosVector();
        //(B-A)*3*(1-u)^2 + (C-B)*6*(1-u)*u + (D-C)*3*u^2
        (B.sub(A, point))
                .scale((float) (3 * (1 - u) * (1 - u)), point)
                .add(C.sub(B, new PosVector()).scale((float) (6 * (1 - u) * u), new PosVector()), direction)
                .add(D.sub(C, new PosVector()).scale((float) (3 * u * u), new PosVector()), direction);
        return direction;
    }

    /**
     * defines a quad in rotational order
     * the vectors do not have to be clockwise
     *
     * @param A      (0, 0)
     * @param B      (0, 1)
     * @param C      (1, 1)
     * @param D      (1, 0)
     * @param normal the normal of this plane
     * @throws NullPointerException if any of the vectors is null
     */
    public void addQuad(PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal){
        DirVector currentNormal = Triangle.getNormalVector(A, B, C);

        if (currentNormal.dot(normal) >= 0){
            addFinalQuad(A, B, C, D, normal);
        } else {
            addFinalQuad(D, C, B, A, normal);
        }
    }

    /** a quad in rotational, counterclockwise order */
    private void addFinalQuad(PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal) {
        addFinalTriangle(A, C, B, normal);
        addFinalTriangle(A, D, C, normal);
    }

    /**
     * defines a quad that is mirrored over the xz-plane
     *
     * @see CustomShape#addFinalQuad(PosVector, PosVector, PosVector, PosVector, DirVector)
     */
    public void addQuad(PosVector A, PosVector B) {
        addQuad(A, B, B.mirrorY(new PosVector()), A.mirrorY(new PosVector()));
    }

    /**
     * @see CustomShape#addFinalQuad(PosVector, PosVector, PosVector, PosVector, DirVector)
     */
    public void addQuad(PosVector A, PosVector B, PosVector C, PosVector D) {
        DirVector normal = Plane.getNormalVector(A, B, C);

        final DirVector direction = middle.to(B, new DirVector());
        if (normal.dot(direction) >= 0) {
            addFinalQuad(A, B, C, D, normal);
        } else {
            normal.negate();
            addFinalQuad(D, C, B, A, normal);
        }
    }

    /**
     * Adds a quad which is mirrored in the XZ-plane
     */
    public void addMirrorQuad(PosVector A, PosVector B, PosVector C, PosVector D) {
        addQuad(A, B, C, D);
        addQuad(
                A.mirrorY(new PosVector()),
                B.mirrorY(new PosVector()),
                C.mirrorY(new PosVector()),
                D.mirrorY(new PosVector())
        );
    }

    /**
     * @see CustomShape#addFinalTriangle(PosVector, PosVector, PosVector, DirVector)
     */
    public void addTriangle(PosVector A, PosVector B, PosVector C) {
        DirVector normal = Plane.getNormalVector(A, B, C);
        final DirVector direction = middle.to(B, new DirVector());

        if (normal.dot(direction) >= 0) {
            addFinalTriangle(A, B, C, normal);
        } else {
            normal.negate();
            addFinalTriangle(C, B, A, normal);
        }
    }


    public void addTriangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        DirVector currentNormal = Triangle.getNormalVector(A, B, C);

        if (currentNormal.dot(normal) >= 0){
            addFinalTriangle(A, B, C, normal);
        } else {
            addFinalTriangle(C, B, A, normal);
        }
    }

    /**
     * defines a triangle with the given points in counterclockwise ordering
     *
     * @see CustomShape#addQuad(PosVector, PosVector, PosVector, PosVector)
     */
    private void addFinalTriangle(PosVector A, PosVector B, PosVector C, DirVector normal) {
        int aInd = addHitpoint(A);
        int bInd = addHitpoint(B);
        int cInd = addHitpoint(C);
        int nInd = addNormal(normal);
        faces.add(new Mesh.Face(aInd, bInd, cInd, nInd));
    }

    private int addNormal(DirVector normal) {
        if (normal == null || normal.equals(DirVector.zeroVector()))
            throw new IllegalArgumentException("Customshape.addNormal(DirVector): invalid normal: " + normal);

        normals.add(normal);
        return normals.size() - 1;
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
        addTriangle(A.mirrorY(new PosVector()), B.mirrorY(new PosVector()), C.mirrorY(new PosVector()));
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
        if (startNormal.dot(A2.to(middle, new DirVector())) > 0) startNormal = startNormal.scale(-1, new DirVector());
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
            newNormal = newNormal.dot(normal.previous()) > 0 ? newNormal : newNormal.scale(-1, new DirVector());
            normal.update(newNormal);

            addFinalQuad(left.previous(), right.previous(), right.current(), left.current(), normal.current());
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
        PosVector B = start.middleTo(M, new PosVector());
        PosVector C = end.middleTo(M, new PosVector());
        return addBezierStrip(start, start.mirrorY(new PosVector()), B, B.mirrorY(new PosVector()), C, C.mirrorY(new PosVector()), end, end.mirrorY(new PosVector()), slices);
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
            DirVector normal = Plane.getNormalVector(point, targets.previous(), targets.current());
            addFinalTriangle(targets.previous(), targets.current(), point, normal);
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
        // this is the most clear, structured way of the duplicate-vector problem. maybe not the most efficient.
        PosVector[] sortedVertices = new PosVector[points.size()];
        points.forEach((v, i) -> sortedVertices[i] = v);

        List<PosVector> vertexList = new ArrayList<>();
        Collections.addAll(vertexList, sortedVertices);

        return new ShapeFromMesh(vertexList, normals, faces);
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

        for (PosVector vec : sortedVertices) {
            writer.println(String.format(Locale.US,"v %1.09f %1.09f %1.09f", vec.x(), vec.z(), vec.y()));
        }

        for (DirVector vec : normals) {
            writer.println(String.format(Locale.US,"vn %1.09f %1.09f %1.09f", vec.x(), vec.z(), vec.y()));
        }

        writer.println("usemtl None");
        writer.println("s off");
        writer.println("");

        for (Mesh.Face face : faces) {
            writer.println("f " + readVertex(face.A) + " " + readVertex(face.B) + " " + readVertex(face.C));
        }

        writer.close();

        Toolbox.print("Successfully created obj file: " + filename);
    }

    private static String readVertex(Pair<Integer, Integer> vertex) {
        return String.format("%d//%d", vertex.left + 1, vertex.right + 1);
    }
}
