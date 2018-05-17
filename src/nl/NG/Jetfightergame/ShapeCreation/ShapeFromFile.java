package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
@SuppressWarnings("unchecked")
public class ShapeFromFile implements Shape {

    /** an arrow along the Z-axis, 1 long */
    private static boolean isLoaded = false;
    public static ShapeFromFile ARROW;
    public static ShapeFromFile CONCEPT_BLUEPRINT;

    /**
     * loads the shapes into memory.
     * This method may be split into several selections of models
     * @param loadMesh whether the meshes should be loaded. If this is false, calling
     * {@link #render(GL2.Painter)} will result in a {@link NullPointerException}
     */
    public static void init(boolean loadMesh){
        if (isLoaded) {
            Toolbox.printError("Tried loading shapes while they where already loaded");
            return;
        }
        CONCEPT_BLUEPRINT = new ShapeFromFile("ConceptBlueprint.obj", loadMesh);
        ARROW = new ShapeFromFile("arrow.obj", loadMesh);
        isLoaded = true;
    }


    private List<PosVector> vertices = Collections.EMPTY_LIST;
    private List<Plane> triangles = Collections.EMPTY_LIST;
    private Mesh mesh;

    private ShapeFromFile(String fileName, boolean loadMesh) {
        this(new ShapeParameters(fileName), loadMesh);
    }

    private ShapeFromFile(ShapeParameters model, boolean loadMesh) {
        this(model.vertices, model.normals, model.faces, loadMesh);

        if (ServerSettings.DEBUG) Toolbox.print("loaded model " + model.name + ": [Faces: " + model.faces.size() + ", vertices: " + model.vertices.size() + "]");
    }

    /**
     * reads a model from the given file.
     * @param loadMesh if true, load a mesh of this file to the GPU. If this is false, calling
     *      * {@link #render(GL2.Painter)} will result in a {@link NullPointerException}
     */
    public ShapeFromFile(List<PosVector> vertices, List<DirVector> normals, List<Mesh.Face> faces, boolean loadMesh) {
        this.vertices = Collections.unmodifiableList(vertices);
        triangles = faces.stream()
                .map(f -> ShapeFromFile.toTriangle(f, this.vertices, normals))
                .collect(Collectors.toList());
        mesh = loadMesh ? new Mesh(this.vertices, normals, faces): null;
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
