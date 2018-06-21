package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Quad;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.*;
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
        this(model.vertices, model.normals, model.faces, loadMesh, GL11.GL_TRIANGLES);

        if (ServerSettings.DEBUG)
            Logger.print("loaded model " + model.name + ": [Faces: " + model.faces.size() + ", vertices: " + model.vertices.size() + "]");
    }

    /**
     * reads a model from the given file.
     * @param loadMesh if true, load a mesh of this file to the GPU. If this is false, calling
     *      * {@link #render(GL2.Painter)} will result in a {@link NullPointerException}
     * @param drawMethod how the shape must be drawn
     */
    public BasicShape(List<PosVector> vertices, List<DirVector> normals, List<Mesh.Face> faces, boolean loadMesh, int drawMethod) {
        this.vertices = Collections.unmodifiableList(vertices);
        this.triangles = faces.stream()
                .map(f -> BasicShape.toPlanes(f, this.vertices, normals))
                .collect(Collectors.toList());
        this.mesh = loadMesh ? new Mesh(this.vertices, normals, faces, drawMethod) : null;
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

    public static List<Shape> loadSplit(String fileName, boolean loadMesh, float containerSize, float scale) {
        // TODO position == (0, 0, 0)
        ShapeParameters file = new ShapeParameters(fileName, new PosVector(0, 0, -400), new int[]{3, 1, 2}, scale);
        HashMap<Vector3i, CustomShape> world = new HashMap<>();

        for (Mesh.Face f : file.faces) {
            PosVector[] edges = new PosVector[f.size()];
            float xMin = Float.MAX_VALUE;
            float yMin = Float.MAX_VALUE;
            float zMin = Float.MAX_VALUE;
            for (int i = 0; i < edges.length; i++) {
                PosVector p = file.vertices.get(f.vert[i]);
                xMin = Math.min(p.x, xMin);
                yMin = Math.min(p.y, yMin);
                zMin = Math.min(p.z, zMin);
                edges[i] = p;
            }

            int x = (int) (xMin / containerSize);
            int y = (int) (yMin / containerSize);
            int z = (int) (zMin / containerSize);

            Vector3i key = new Vector3i(x, y, z);
            CustomShape container = world.computeIfAbsent(key, k -> new CustomShape(new PosVector(x, y, -Float.MAX_VALUE)));
            container.addPlane(null, edges);
        }

        Collection<CustomShape> containers = world.values();
        Logger.print("Loaded model " + file.name + " in " + containers.size() + " parts");
//        Logger.print(world);
        return containers.stream()
                .map(s -> s.wrapUp(loadMesh))
                .collect(Collectors.toList());
    }

    private static float minimum(float... items) {
        float min = -Float.MAX_VALUE;
        for (float item : items) {
            if (item < min) {
                min = item;
            }
        }
        return min;
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
     * creates a plane object, using the indices on the given lists
     *
     * @param posVectors a list where the vertex indices of A, B and C refer to
     * @param normals    a list where the normal indices of A, B and C refer to
     * @return a triangle whose normal is the average of those of A, B and C, in Shape-space
     */
    public static Plane toPlanes(Mesh.Face face, List<PosVector> posVectors, List<DirVector> normals) {
        final PosVector[] border = new PosVector[face.size()];
        Arrays.setAll(border, i -> posVectors.get(face.vert[i]));
        // take average normal as normal of plane, or use default method if none are registered
        DirVector normal = new DirVector();
        for (int index : face.norm) {
            if (index >= 0) {
                normal.add(normals.get(index));
            }
        }

        switch (face.size()) {
            case 3:
                return Triangle.createTriangle(border[0], border[1], border[2], normal);
            case 4:
                return Quad.createQuad(border[0], border[1], border[2], border[3], normal);
            default:
                throw new UnsupportedOperationException("polygons with " + face.size() + " edges are not supported");
        }
    }

    private static DirVector fetchDir(List<DirVector> normals, int index) {
        return (index < 0) ? DirVector.zeroVector() : normals.get(index);
    }

}
