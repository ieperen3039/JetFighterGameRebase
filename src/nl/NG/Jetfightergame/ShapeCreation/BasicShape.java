package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Primitives.Plane;
import nl.NG.Jetfightergame.Primitives.Quad;
import nl.NG.Jetfightergame.Primitives.Triangle;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public BasicShape(ShapeParameters model, boolean loadMesh) {
        this(model.vertices, model.normals, model.faces, loadMesh, GL11.GL_TRIANGLES);

        if (ServerSettings.DEBUG)
            Logger.DEBUG.print("loaded model " + model.name + ": [Faces: " + model.faces.size() + ", vertices: " + model.vertices.size() + "]");
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
        ShapeParameters file = new ShapeParameters(fileName, PosVector.zeroVector(), scale);
        HashMap<Vector3i, CustomShape> world = new HashMap<>();

        for (Mesh.Face f : file.faces) {
            PosVector[] edges = new PosVector[f.size()];
            float xMin = Float.MAX_VALUE;
            float yMin = Float.MAX_VALUE;
            float zMin = Float.MAX_VALUE;
            for (int i = 0; i < f.size(); i++) {
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
            CustomShape container = world.computeIfAbsent(key, k ->
                    new CustomShape(new PosVector(x + 0.5f, y + 0.5f, -Float.MAX_VALUE))
            );

            DirVector normal = new DirVector();
            for (int ind : f.norm) {
                if (ind < 0) continue;
                normal.add(file.normals.get(ind));
            }
            if (normal.isScalable()) {
                normal.normalize();
            } else {
                normal = null;
                Logger.DEBUG.printSpamless(fileName, fileName + " has at least one not-computed normal");
            }

            container.addPlane(normal, edges);
        }

        Collection<CustomShape> containers = world.values();
        Logger.DEBUG.print("Loaded model " + file.name + " in " + containers.size() + " parts");

        List<Shape> shapes = new ArrayList<>();
        for (CustomShape frame : containers) {
            shapes.add(frame.wrapUp(loadMesh));
        }
        return shapes;
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
    public Stream<? extends Plane> getPlaneStream() {
        return triangles.stream();
    }

    @Override
    public Stream<? extends PosVector> getPointStream() {
        return vertices.stream();
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
     * @param vertices a list where the vertex indices of A, B and C refer to
     * @param normals    a list where the normal indices of A, B and C refer to
     * @return a triangle whose normal is the average of those of A, B and C, in Shape-space
     */
    public static Plane toPlanes(Mesh.Face face, List<PosVector> vertices, List<DirVector> normals) {
        final PosVector[] border = new PosVector[face.size()];
        Arrays.setAll(border, i -> vertices.get(face.vert[i]));
        // take average normal as normal of plane, or use default method if none are registered
        DirVector normal = new DirVector();
        for (int index : face.norm) {
            normal.add((index < 0) ? DirVector.zeroVector() : normals.get(index));
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

}
