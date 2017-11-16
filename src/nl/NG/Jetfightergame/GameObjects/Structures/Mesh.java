package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Plane;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Triangle;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.lwjgl.system.MemoryUtil;
import sun.plugin.dom.exception.InvalidStateException;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 */
public class Mesh implements Shape {

    public static final Mesh BASIC = new Mesh("res/Models/ConceptBlueprint.obj", PosVector.O, new int[]{3, 1, 2}, 1f);
    public static final Mesh ARROW = new Mesh("res/Models/Arrow.obj", PosVector.Z, new int[]{3, 1, 2}, 1f);//TODO make

    /** every point of this mesh exactly once */
    private final List<PosVector> vertices;
    /** every plane on this mesh (triangles) */
    private final List<Triangle> triangles;

    private int vaoId;
    private int vertexCount;
    private int posVboID;
    private int idVboID;
    private int normVboID;

    private boolean isDrawable = false;
    /** every normal exactly once */
    private List<DirVector> normals;
    private IntBuffer vboBuffers;

    /**
     * @param fileName path to the .obj file
     * @param offSet offset of the gravity middle in this mesh as {@code GM * -1}
     * @param XYZ determines the definition of the axes. maps {forward, right, up} for X=1, Y=2, Z=3.
     * @param scale the scale applied to this object
     */
    private Mesh(String fileName, PosVector offSet, int[] XYZ, float scale) {
        this(new MeshParameters(fileName, offSet, XYZ, scale), fileName) ;
    }

    private Mesh(MeshParameters par, String name){
        this(par.vertices, par.normals, par.faces);
        Toolbox.print("loaded mesh " + name + ": [Faces: " + par.faces.size() + ", vertices: " + par.vertices.size() + "]");
    }

    /**
     * create a mesh, and bind it to the GPU. the list of vertices is contained in this object and should not
     * externally be modified
     * @param vertices a list of vertices, where preferably every vertex occurs only once.
     * @param normals a list of normals, where preferably every normal occurs only once
     * @param faces a collection of indices mapping to 3 vertices and corresponding normals
     */
    public Mesh(List<PosVector> vertices, List<DirVector> normals, List<Face> faces) {
        this.vertices = vertices;
        triangles = faces.stream()
            .map(f -> f.toTriangle(vertices, normals))
            .collect(Collectors.toCollection(LinkedList::new));

        createDrawable(vertices, normals, faces);
    }

    private void createDrawable(List<PosVector> posList, List<DirVector> normList, List<Face> facesList) {

        List<Integer> indices = new ArrayList<>();
        // Create position array in the order it has been declared
        float[] posArr = new float[posList.size() * 3];

        for (int i = 0; i < posList.size(); i++) {
            putInList(posList, posArr, i);
        }

        float[] normArr = new float[posList.size() * 3];

        for (Face face : facesList) {
            Pair<Integer, Integer> alpha = face.A;
            Pair<Integer, Integer> beta = face.B;
            Pair<Integer, Integer> gamma = face.C; // TODO enforce flat shading
            processFaceVertex(normList, indices, normArr, alpha.left, alpha.right);
            processFaceVertex(normList, indices, normArr, beta.left, beta.right);
            processFaceVertex(normList, indices, normArr, gamma.left, gamma.right);
        }

        int[] indicesArr;
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        writeToGL(posArr, normArr, indicesArr);
    }

    private void putInList(List<? extends Vector> sourceList, float[] array, int i) {
        Vector vec = sourceList.get(i);
        array[i * 3] = (float) vec.x();
        array[i * 3 + 1] = (float) vec.y();
        array[i * 3 + 2] = (float) vec.z();
    }

    private void processFaceVertex(List<DirVector> normList, List<Integer> indicesList, float[] normArr, int posIndex, Integer normalIndex) {
        // Set index for vertex coordinates
        indicesList.add(posIndex);

        if (normalIndex >= 0) {
            putInList(normList, normArr, normalIndex);
        }
    }

    private void writeToGL(float[] positions, float[] normals, int[] indices) {
            FloatBuffer posBuffer = null;
            FloatBuffer norBuffer = null;
            IntBuffer indicesBuffer = null;
            try {

                vertexCount = indices.length;

                vaoId = glGenVertexArrays();
                glBindVertexArray(vaoId);

                // Position VBO
                posVboID = glGenBuffers();
                posBuffer = MemoryUtil.memAllocFloat(positions.length);
                posBuffer.put(positions).flip();
                glBindBuffer(GL_ARRAY_BUFFER, posVboID);
                glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

                // Vertex normals VBO
                normVboID = glGenBuffers();
                norBuffer = MemoryUtil.memAllocFloat(normals.length);
                norBuffer.put(normals).flip();
                glBindBuffer(GL_ARRAY_BUFFER, normVboID);
                glBufferData(GL_ARRAY_BUFFER, norBuffer, GL_STATIC_DRAW);
                glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

                // Index VBO
                idVboID = glGenBuffers();
                indicesBuffer = MemoryUtil.memAllocInt(indices.length);
                indicesBuffer.put(indices).flip();
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idVboID);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
            } finally {
                if (posBuffer != null) {
                    MemoryUtil.memFree(posBuffer);
                }
                if (norBuffer != null) {
                    MemoryUtil.memFree(norBuffer);
                }
                if (indicesBuffer != null) {
                    MemoryUtil.memFree(indicesBuffer);
                }
            }
        }

    public void cleanup() {

        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboID);
        glDeleteBuffers(idVboID);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    @Override
    public Stream<? extends Plane> getPlanes() {
        return triangles.stream();
    }

    @Override
    public Collection<PosVector> getPoints() {
        return vertices;
    }

    @Override
    public void draw(GL2 gl) {
        if (!isDrawable) throw new InvalidStateException("Mesh.draw(GL) was called before createDrawable(GL)");

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    public static class Face {
        /**
         * boundary in no particular order. each pair consists of (left vertex index, right normal index)
         */
        Pair<Integer, Integer> A, B, C;
        public Triangle result = null;

        /**
         * for storage of vertex-indices
         * face == plane
         */
        private Face(String v1, String v2, String v3) {
            A = (parseVertex(v1));
            B = (parseVertex(v2));
            C = (parseVertex(v3));
        }

        /**
         * a description of a plane.
         * Every parameter has on left the index of a vertex in some list,
         * on right the index of a normal in some list.
         * it has no value to combine these lists
         */
        public Face(Pair<Integer, Integer> a, Pair<Integer, Integer> b, Pair<Integer, Integer> c) {
            A = a;
            B = b;
            C = c;
        }

        /**
         * a description of a plane
         * this constructor takes 3 vertex indices and a single normal index.
         * @param normal the index for the normal of each of these vertices
         */
        public Face(int a, int b, int c, int normal){
            this(new Pair<>(a, normal), new Pair<>(b, normal), new Pair<>(c, normal));
        }

        /** parse and store the references of a single vertex */
        private static Pair<Integer, Integer> parseVertex(String line) {
            String[] lineTokens = line.split("/");
            int vertex = Integer.parseInt(lineTokens[0]) - 1;

            if (lineTokens.length > 2) {
                return new Pair<>(vertex, Integer.parseInt(lineTokens[2]) - 1);
            }
            return new Pair<>(vertex, -1);
        }

        /**
         * creates a triangle object, using the indices on the given lists
         * @param posVectors a list where the vertex indices of A, B and C refer to
         * @param normals a list where the normal indices of A, B and C refer to
         * @return a triangle whose normal is the average of those of A, B and C, in Shape-space
         */
        public Triangle toTriangle(List<PosVector> posVectors, List<DirVector> normals) {
            
            if (result == null) {
                
                final PosVector alpha = posVectors.get(A.left);
                final PosVector beta = posVectors.get(B.left);
                final PosVector gamma = posVectors.get(C.left);

                // take average normal as normal of plane, or use default method if none are registered
                DirVector normal = fetchDir(normals, A.right)
                        .add(fetchDir(normals, B.right))
                        .add(fetchDir(normals, C.right));
                if (!normal.isScalable()) normal = Plane.getNormalVector(alpha, beta, gamma, PosVector.O);
                
                this.result = new Triangle(alpha, beta, gamma, normal);
            }
            
            return result;
        }

        private static DirVector fetchDir(List<DirVector> normals, int index) {
            return index < 0 ? DirVector.O : normals.get(index);
        }
    }

    private static class MeshParameters {
        private List<PosVector> vertices;
        private List<DirVector> normals;
        private List<Face> faces;

        private MeshParameters(String fileName, PosVector offSet, int[] XYZ, float scale) {
            vertices = new ArrayList<>();
            normals = new ArrayList<>();
            faces = new LinkedList<>();
            List<String> lines = openMesh(fileName);

            for (String line : lines) {
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v":
                        // Geometric vertex
                        PosVector vec3f = new PosVector(
                                Float.parseFloat(tokens[XYZ[0]]),
                                Float.parseFloat(tokens[XYZ[1]]),
                                Float.parseFloat(tokens[XYZ[2]]))
                                .scale(scale)
                                .add(offSet)
                                .toPosVector();
                        vertices.add(vec3f);
                        break;
                    case "vn":
                        // Vertex normal
                        DirVector vec3fNorm = new DirVector(
                                Float.parseFloat(tokens[1]),
                                Float.parseFloat(tokens[2]),
                                Float.parseFloat(tokens[3]));
                        normals.add(vec3fNorm);
                        break;
                    case "f":
                        faces.add(new Face(tokens[1], tokens[2], tokens[3]));
                        break;
                    default:
                        // Ignore other lines
                        break;
                }
            }
        }

        private List<String> openMesh(String fileName) {
            try {
                return Files.readAllLines(Paths.get(fileName));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Could not read mesh! Continuing game without model...");
                return new LinkedList<>();
            }
        }
    }
}
