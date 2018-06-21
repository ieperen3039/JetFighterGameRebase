package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
@SuppressWarnings("unchecked")
public class BasicShape implements Shape {

    private List<PosVector> vertices = Collections.EMPTY_LIST;
    private List<Plane> triangles = Collections.EMPTY_LIST;
    private Mesh mesh;

    public BasicShape(String fileName, boolean loadMesh) {
        this(new ShapeParameters(fileName), loadMesh);
    }

    private BasicShape(ShapeParameters model, boolean loadMesh) {
        this(model.vertices, model.normals, model.faces, loadMesh);

        if (ServerSettings.DEBUG)
            Logger.print("loaded model " + model.name + ": [Faces: " + model.faces.size() + ", vertices: " + model.vertices.size() + "]");
    }

    /**
     * reads a model from the given file.
     * @param loadMesh if true, load a mesh of this file to the GPU. If this is false, calling
     *      * {@link #render(GL2.Painter)} will result in a {@link NullPointerException}
     */
    public BasicShape(List<PosVector> vertices, List<DirVector> normals, List<Mesh.Face> faces, boolean loadMesh) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.triangles = faces.stream()
                .map(f -> BasicShape.toTriangle(f, this.vertices, normals))
                .collect(Collectors.toList());
        this.mesh = loadMesh ? new Mesh(this.vertices, normals, faces) : null;
    }

    private BasicShape(List<Plane> triangles, Mesh mesh) {
        vertices = new ArrayList<>();
        this.triangles = triangles;
        this.mesh = mesh;

        for (Plane t : triangles) {
            for (PosVector posVector : t.getBorder()) {
                vertices.add(posVector);
            }
        }
    }

    public static List<Shape> loadSplit(String fileName, boolean loadMesh, float containerSize) {
        ShapeParameters file = new ShapeParameters(fileName);
        HashMap<int[], CustomShape> world = new HashMap<>();

        for (Mesh.Face f : file.faces) {
            final PosVector alpha = file.vertices.get(f.A.left);
            final PosVector beta = file.vertices.get(f.B.left);
            final PosVector gamma = file.vertices.get(f.C.left);

            // take average normal as normal of plane, or use default method if none are registered
            float xMin = minimum(alpha.x, beta.x, gamma.x);
            float yMin = minimum(alpha.y, beta.y, gamma.y);
            float zMin = minimum(alpha.z, beta.z, gamma.z);

            int x = (int) (xMin / containerSize);
            int y = (int) (yMin / containerSize);
            int z = (int) (zMin / containerSize);

            int[] key = {x, y, z};
            CustomShape container = world.computeIfAbsent(key, k -> new CustomShape(new PosVector(x, y, -Float.MAX_VALUE)));
            container.addTriangle(alpha, beta, gamma);
        }

        return world.values().stream()
                .map(s -> s.wrapUp(loadMesh))
                .collect(Collectors.toList());
    }

    private static float minimum(float a, float b, float c) {
        if (a < b)
            return (a < c) ? a : c;
        else
            return (b < c) ? b : c;
    }

    @Override
    public Iterable<? extends Plane> getPlanes() {
        return Collections.unmodifiableList(triangles);
    }

    @Override
    public Iterable<PosVector> getPoints() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public void render(GL2.Painter lock) {
        mesh.render(lock);
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    /**
     * creates a triangle object, using the indices on the given lists
     *
     * @param posVectors a list where the vertex indices of A, B and C refer to
     * @param normals    a list where the normal indices of A, B and C refer to
     * @return a triangle whose normal is the average of those of A, B and C, in Shape-space
     */
    public static Triangle toTriangle(Mesh.Face face, List<PosVector> posVectors, List<DirVector> normals) {
        final PosVector alpha = posVectors.get(face.A.left);
        final PosVector beta = posVectors.get(face.B.left);
        final PosVector gamma = posVectors.get(face.C.left);

        // take average normal as normal of plane, or use default method if none are registered
        DirVector normal = new DirVector();
        fetchDir(normals, face.A.right)
                .add(fetchDir(normals, face.B.right), normal)
                .add(fetchDir(normals, face.C.right), normal);
        if (!normal.isScalable()) normal = Plane.getNormalVector(alpha, beta, gamma);

        return Triangle.createTriangle(alpha, beta, gamma, normal);
    }

    private static DirVector fetchDir(List<DirVector> normals, int index) {
        return (index < 0) ? DirVector.zeroVector() : normals.get(index);
    }

}
