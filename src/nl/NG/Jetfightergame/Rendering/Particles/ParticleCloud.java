package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * a group of particles, each rendered around the same time
 * @author Geert van Ieperen created on 16-5-2018.
 */
public class ParticleCloud {

    private int vaoId;
    private int posRelVboID;
    private int posMidVboID;
    private int rotVboID;
    private int moveVboID;
    private int colorVboID;

    private ArrayList<Particle> bulk = new ArrayList<>();

    private void addParticle(PosVector A, PosVector B, PosVector C, DirVector movement, Vector3f angVec, Color4f color, float rotationSpeed, float timeToLive) {
        bulk.add(new Particle(A, B, C, movement, color, angVec, rotationSpeed, timeToLive));
    }

    public void writeToGL(float currentTime) {
        int n = bulk.size();

        FloatBuffer posRelBuffer = MemoryUtil.memAllocFloat(3 * 3 * n);
        FloatBuffer posMidBuffer = MemoryUtil.memAllocFloat(3 * 3 * n);
        FloatBuffer rotBuffer = MemoryUtil.memAllocFloat(3 * 4 * n);
        FloatBuffer moveBuffer = MemoryUtil.memAllocFloat(3 * 3 * n);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(3 * 4 * n);
        FloatBuffer ttlBuffer = MemoryUtil.memAllocFloat(3 * n);

        for (Particle p : bulk) {
            for (int i = 0; i < 3; i++) { // every vertex must have its own copy of the attributes
                posRelBuffer.put(p.sides[i].toFloatBuffer());
                posMidBuffer.put(p.position.toFloatBuffer());
                addAxisAngle(rotBuffer, p.rotation);
                moveBuffer.put(p.movement.toFloatBuffer());
                colorBuffer.put(p.color.toFloatBuffer());
                ttlBuffer.put(p.timeToLive + currentTime);
            }
        }

        posRelBuffer.flip();
        posMidBuffer.flip();
        rotBuffer.flip();
        moveBuffer.flip();
        colorBuffer.flip();

        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posRelVboID = loadToGL(posRelBuffer, 0, 3); // Position VBO
            posMidVboID = loadToGL(posMidBuffer, 1, 3); // Vertex normals VBO
            rotVboID = loadToGL(rotBuffer, 2, 4); // Rotation VBO
            moveVboID = loadToGL(moveBuffer, 3, 3); // Movement VBO
            colorVboID = loadToGL(colorBuffer, 4, 4); // Color VBO

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            MemoryUtil.memFree(posRelBuffer);
            MemoryUtil.memFree(posMidBuffer);
            MemoryUtil.memFree(rotBuffer);
            MemoryUtil.memFree(moveBuffer);
            MemoryUtil.memFree(colorBuffer);
        }

        bulk.clear();
    }

    public static int loadToGL(FloatBuffer buffer, int index, int itemSize) {
        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(index, itemSize, GL_FLOAT, false, 0, 0);
        return vboID;
    }

    private static void addAxisAngle(FloatBuffer rotBuffer, AxisAngle4f rot) {
        rotBuffer.put(new float[]{rot.x, rot.y, rot.z, rot.angle});
    }

    /**
     * renders all particles. The particle-shader must be linked first
     */
    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glBindVertexArray(0);
    }

    public void dispose() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posRelVboID);
        glDeleteBuffers(posMidVboID);
        glDeleteBuffers(rotVboID);
        glDeleteBuffers(moveVboID);
        glDeleteBuffers(colorVboID);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    private class Particle {
        public final PosVector[] sides;
        public final PosVector position;
        public final DirVector normal;
        public final DirVector movement;
        public final Color4f color;
        public final AxisAngle4f rotation;
        public final float timeToLive;

        public Particle(PosVector A, PosVector B, PosVector C, DirVector movement, Color4f color, Vector3f angVec, float rotationSpeed, float timeToLive) {
            this.sides = new PosVector[]{A, B, C};
            this.position = new PosVector();
            this.position.set(A).add(B).add(C).div(3);
            this.normal = Triangle.getNormalVector(A, B, C);
            this.movement = movement;
            this.color = color;
            this.rotation = new AxisAngle4f(rotationSpeed, angVec);
            this.timeToLive = timeToLive;
        }

    }
}
