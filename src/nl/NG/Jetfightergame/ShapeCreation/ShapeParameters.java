package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ShapeParameters {

    public List<PosVector> vertices;
    public List<DirVector> normals;
    public List<Mesh.Face> faces;
    public final String name;

    /**
     * @param fileName path to the .obj file
     */
    public ShapeParameters(String fileName) {
        this(fileName, PosVector.zeroVector(), new int[]{3, 1, 2}, 1f);
    }

    /**
     * @param fileName path from the directory main to the .obj file
     * @param offSet   offset of the gravity middle in this mesh as {@code GM * -1}
     * @param XYZ      determines the definition of the axes. maps {forward, right, up} for X=1, Y=2, Z=3.
     * @param scale    the scale standard applied to this object, to let it correspond to its contract
     */
    private ShapeParameters(String fileName, PosVector offSet, int[] XYZ, float scale) {
        name = asName(fileName);
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        faces = new ArrayList<>();

        List<String> lines = openMesh(Directory.meshes, fileName);

        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    PosVector vec3f = new PosVector();
                    new PosVector(
                            Float.parseFloat(tokens[XYZ[0]]),
                            Float.parseFloat(tokens[XYZ[1]]),
                            Float.parseFloat(tokens[XYZ[2]]))
                            .scale(scale, vec3f)
                            .add(offSet, vec3f);
                    vertices.add(vec3f);
                    break;
                case "vn":
                    // Vertex normal
                    DirVector vec3fNorm = new DirVector(
                            Float.parseFloat(tokens[XYZ[0]]),
                            Float.parseFloat(tokens[XYZ[1]]),
                            Float.parseFloat(tokens[XYZ[2]]));
                    normals.add(vec3fNorm);
                    break;
                case "f":
                    faces.add(makeFace(tokens[1], tokens[2], tokens[3]));
                    break;
                default:
                    // Ignore other lines
                    break;
            }
        }

        if (vertices.isEmpty() || faces.isEmpty()) {
            System.err.println("Empty mesh loaded: "+fileName+" (this may result in errors)");
        }
    }

    public static String asName(String fileName) {
        return fileName.replace(".obj", "");
    }

    private static List<String> openMesh(Directory dir, String fileName) {
        try {
            return Files.readAllLines(dir.getPath(fileName));
        } catch (IOException e) {
            System.err.println("Could not read mesh '" + fileName + "'. Continuing game without model.");
            if (ServerSettings.DEBUG) e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * for storage of vertex-indices face == plane
     */
    private static Mesh.Face makeFace(String v1, String v2, String v3) {
        Pair<Integer, Integer> a = (parseVertex(v1));
        Pair<Integer, Integer> b = (parseVertex(v2));
        Pair<Integer, Integer> c = (parseVertex(v3));
        return new Mesh.Face(a, b, c);
    }

    /**
     * parse and store the references of a single vertex
     */
    private static Pair<Integer, Integer> parseVertex(String line) {
        String[] lineTokens = line.split("/");
        int vertex = Integer.parseInt(lineTokens[0]) - 1;

        if (lineTokens.length > 2) {
            return new Pair<>(vertex, Integer.parseInt(lineTokens[2]) - 1);
        }
        return new Pair<>(vertex, -1);
    }
}
