package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
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

    private int vaoId;
    private int vertexCount;
    private int posVboID;
    private int idVboID;
    private int normVboID;

    public Mesh(List<PosVector> posList, List<DirVector> normList, List<Face> facesList) {

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
    public void render(GL2.Painter lock) {
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
}
