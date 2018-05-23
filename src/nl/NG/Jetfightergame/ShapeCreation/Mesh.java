package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.Renderable;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Geert van Ieperen
 *         created on 17-11-2017.
 */
public class Mesh implements Renderable {
    private static Collection<Mesh> loadedMeshes = new ArrayList<>(20);
    public static Mesh EMPTY_MESH = new EmptyMesh();

    private int vaoId;
    private int vertexCount;
    private int posVboID;
    private int normVboID;

    /**
     * VERY IMPORTANT that you have first called GLFW windowhints (or similar) for openGL 3 or higher.
     */
    public Mesh(List<PosVector> posList, List<DirVector> normList, List<Face> facesList) {
        // Create position array in the order it has been declared. faces have 3 vertices of 3 indices
        float[] posArr = new float[facesList.size() * 9];
        float[] normArr = new float[facesList.size() * 9];

        for (int i = 0; i < facesList.size(); i++) {
            Face face = facesList.get(i);
            readFaceVertex(posList, posArr, i, face);
            readFaceNormals(normList, normArr, i, face);
        }

        writeToGL(posArr, normArr);
        loadedMeshes.add(this);
    }

    private void readFaceVertex(List<PosVector> posList, float[] posArr, int faceNumber, Face face) {
        int indices = faceNumber * 3;
        readVector(indices, posList, posArr, face.A.left);
        readVector(indices + 1, posList, posArr, face.B.left);
        readVector(indices + 2, posList, posArr, face.C.left);
    }

    private void readFaceNormals(List<DirVector> normList, float[] normArr, int faceNumber, Face face) {
        int indices = faceNumber * 3;
        readVector(indices, normList, normArr, face.A.right);
        readVector(indices + 1, normList, normArr, face.B.right);
        readVector(indices + 2, normList, normArr, face.C.right);
    }

    private static void readVector(int vectorNumber, List<? extends Vector> sourceList, float[] targetArray, int index) {
        Vector vertex = sourceList.get(index);
        int arrayPosition = vectorNumber * 3;
        targetArray[arrayPosition] = vertex.x();
        targetArray[arrayPosition + 1] = vertex.y();
        targetArray[arrayPosition + 2] = vertex.z();
    }


    /**
     * create a mesh and store it to the GL. For both lists it holds that the ith vertex has the ith normal vector
     * @param positions the vertices, concatenated in groups of 3
     * @param normals the normals, concatenated in groups of 3
     * @throws IllegalArgumentException if any of the arrays has length not divisible by 3
     * @throws IllegalArgumentException if the arrays are of unequal length
     */
    private void writeToGL(float[] positions, float[] normals) {
        if (((positions.length % 3) != 0) || (positions.length == 0)) {
            throw new IllegalArgumentException("received invalid position array of length " + positions.length + ".");
        } else if (normals.length != positions.length) {
            throw new IllegalArgumentException("received a normals array that is not as long as positions: " +
                    positions.length + " position values and " + normals.length + "normal values");
        }

        vertexCount = positions.length;
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(vertexCount);
        FloatBuffer normBuffer = MemoryUtil.memAllocFloat(vertexCount);

        try {
            posBuffer.put(positions).flip();
            normBuffer.put(normals).flip();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            posVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, posVboID);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Vertex normals VBO
            normVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normVboID);
            glBufferData(GL_ARRAY_BUFFER, normBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            
        } finally {
            MemoryUtil.memFree(posBuffer);
            MemoryUtil.memFree(normBuffer);
        }
    }

    @Override
    public void render(GL2.Painter lock) {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    /**
     * all meshes that have been written to the GPU will be removed
     */
    public static void cleanAll() {
        loadedMeshes.forEach(Mesh::dispose);
        Toolbox.checkGLError();
    }

    @Override
    public void dispose() {
        glDisableVertexAttribArray(0);

        glDeleteBuffers(posVboID);
        glDeleteBuffers(normVboID);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /** allows for an empty mesh */
    private Mesh(){}

    /**
     * a record class to describe a plane by indices
     * {@link #Face(int, int, int, int)}
     */
    public static class Face {
        /**
         * boundary in no particular order. each pair consists of (left vertex index, right normal index)
         */
        Pair<Integer, Integer> A, B, C;

        /**
         * a description of a plane
         * this constructor takes 3 vertex indices and a single normal index.
         * @param normal the index for the normal of each of these vertices
         */
        public Face(int a, int b, int c, int normal){
            this(new Pair<>(a, normal), new Pair<>(b, normal), new Pair<>(c, normal));
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
    }

    /**
     * an error replacement
     */
    private static class EmptyMesh extends Mesh {
        public EmptyMesh() {
            super();
        }
        @Override
        public void render(GL2.Painter lock) {}
        @Override
        public void dispose() {}
    }
}
