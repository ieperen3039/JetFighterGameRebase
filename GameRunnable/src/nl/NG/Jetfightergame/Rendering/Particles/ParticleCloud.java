package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static nl.NG.Jetfightergame.Settings.ClientSettings.PARTICLECLOUD_MIN_TIME;
import static nl.NG.Jetfightergame.Settings.ClientSettings.PARTICLECLOUD_SPLIT_SIZE;
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
    private int ttlVboID;

    private ArrayList<Particle> bulk = new ArrayList<>();
    private float maxTTL = 0;
    private float minTTL = Float.MAX_VALUE;
    private int vertexCount;

    /**
     * @throws NullPointerException if {@link #writeToGL(float)} has been called before this method
     * @param A a vertex of the particle. Order doesn't matter
     * @param B another vertex of the particle
     * @param C another vertex of the particle
     * @param movement the displacement of the middle of the particle in one second
     * @param angVec the particle rotates around this angle
     * @param color particle color
     * @param rotationSpeed the particle makes (this/2pi) rotations per second
     * @param timeToLive actual time to live
     */
    public void addParticle(PosVector A, PosVector B, PosVector C, DirVector movement, Vector3f angVec, Color4f color, float rotationSpeed, float timeToLive) {
        addParticle(new Particle(A, B, C, movement, color, angVec, rotationSpeed, timeToLive));
    }

    /**
     *
     * @throws NullPointerException if {@link #writeToGL(float)} has been called before this method
     * @param position position of the middle of the particle
     * @param direction the direction where this particle moves to
     * @param jitter a random speed at which this particle actual direction is offset to direction.
*               If direction is the zero vector, the actual speed will be random linear distributed between this and 0
     * @param maxTTL maximum time to live. Actual time will be random quadratic distributed between this and 0
     * @param color color of this particle
     * @param particleSize
     */
    public void addParticle(PosVector position, DirVector direction, float jitter, float maxTTL, Color4f color, float particleSize) {
        final float randFloat = Toolbox.random.nextFloat();
        final DirVector random = DirVector.randomOrb();

        final float rotationSpeed = 2 + (2 / randFloat);
        random.mul(randFloat * jitter * direction.length()).add(direction);

        addParticle(position, random, color, DirVector.random(), rotationSpeed, randFloat * randFloat * maxTTL, particleSize);
    }

    public void addParticle(PosVector position, DirVector movement, Color4f color, DirVector rotationVector, float rotationSpeed, float timeToLive, float particleSize) {
        addParticle(new Particle(position, movement, color, rotationVector, rotationSpeed, timeToLive, particleSize));
    }

    private void addParticle(Particle p) {
        bulk.add(p);
        maxTTL = Math.max(maxTTL, p.timeToLive);
        minTTL = Math.min(minTTL, p.timeToLive);
    }

    /**
     * @param currentTime initial time in seconds
     */
    public void writeToGL(float currentTime) {
        float despawnPeriod = maxTTL - minTTL;

        if ((despawnPeriod > PARTICLECLOUD_MIN_TIME) && (bulk.size() > PARTICLECLOUD_SPLIT_SIZE)) {
            float mid = minTTL + (despawnPeriod / 2);
            splitOff(mid, currentTime);
        }

        int n = bulk.size();
        maxTTL += currentTime;
        minTTL += currentTime;

        vertexCount = 3 * n;
        FloatBuffer posRelBuffer = MemoryUtil.memAllocFloat(3 * vertexCount);
        FloatBuffer posMidBuffer = MemoryUtil.memAllocFloat(3 * vertexCount);
        FloatBuffer rotBuffer = MemoryUtil.memAllocFloat(4* vertexCount);
        FloatBuffer moveBuffer = MemoryUtil.memAllocFloat(3 * vertexCount);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(4 * vertexCount);
        FloatBuffer ttlBuffer = MemoryUtil.memAllocFloat(2 * vertexCount);

        for (Particle p : bulk) {
            for (int i = 0; i < 3; i++) { // every vertex must have its own copy of the attributes
                posRelBuffer.put(p.sides[i].toFloatBuffer());
                posMidBuffer.put(p.position.toFloatBuffer());
                addAxisAngle(rotBuffer, p.rotation);
                moveBuffer.put(p.movement.toFloatBuffer());
                colorBuffer.put(p.color.toFloatBuffer());
                ttlBuffer.put(currentTime);
                ttlBuffer.put(currentTime + p.timeToLive);
            }
        }

        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            posRelVboID = loadToGL(posRelBuffer, 0, 3); // Position relative to middle VBO
            posMidVboID = loadToGL(posMidBuffer, 1, 3); // Position of triangle middle VBO
            rotVboID = loadToGL(rotBuffer, 2, 4); // Rotation VBO
            moveVboID = loadToGL(moveBuffer, 3, 3); // Movement VBO
            colorVboID = loadToGL(colorBuffer, 4, 4); // Color VBO
            ttlVboID = loadToGL(ttlBuffer, 5, 2); // beginTime-maxTTL VBO

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            MemoryUtil.memFree(posRelBuffer);
            MemoryUtil.memFree(posMidBuffer);
            MemoryUtil.memFree(rotBuffer);
            MemoryUtil.memFree(moveBuffer);
            MemoryUtil.memFree(colorBuffer);
            MemoryUtil.memFree(ttlBuffer);
        }

        Toolbox.checkGLError();
        bulk = null;
    }

    /** a rough estimate of the number of visible particles, assuming a quadratic despawn rate */
    public int estParticlesAt(float currentTime){
        if (bulk != null) return 0;

        float fraction = 1 - ((currentTime - minTTL) / (maxTTL - minTTL));
        if (fraction < 0) return 0;
        if (fraction > 1) fraction = 1;

        return (int) ((vertexCount / 3) * fraction * fraction); // assuming quadratic despawn
    }

    /**
     * splits off all particles with timeToLive more than the given TTL.
     * The new particles are written to GL
     * @param newTTL the new maximum ttl of these particles
     * @param currentTime
     */
    private void splitOff(float newTTL, float currentTime) {
        ParticleCloud newCloud = new ParticleCloud();
        ArrayList<Particle> newBulk = new ArrayList<>();

        for (Particle p : bulk) {
            if (p.timeToLive > newTTL){
                newCloud.addParticle(p);
            } else {
                newBulk.add(p);
            }
        }

        newCloud.writeToGL(currentTime);

        maxTTL = newTTL;
        bulk = newBulk;
    }

    private static int loadToGL(FloatBuffer buffer, int index, int itemSize) {
        buffer.flip();
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
     * renders all particles. The particle-shader must be linked first, and writeToGl must be called
     */
    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0); // Position relative to middle VBO
        glEnableVertexAttribArray(1); // Position of triangle middle VBO
        glEnableVertexAttribArray(2); // Rotation VBO
        glEnableVertexAttribArray(3); // Movement VBO
        glEnableVertexAttribArray(4); // Color VBO
        glEnableVertexAttribArray(5); // TTL VBO

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glBindVertexArray(0);
    }

    public boolean hasFaded(float currentTime){
        return currentTime > maxTTL;
    }

    public boolean disposeIfFaded(float currentTime) {
        if (hasFaded(currentTime)){
            dispose();
            return true;
        }
        return false;
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
        glDeleteBuffers(ttlVboID);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /**
     * merges the other queued particles into this particle
     * Note! this only works before {@link #writeToGL(float)} is called
     * @param other another particle cloud. The other will not be modified
     * @throws NullPointerException if this or the other has called {@link #writeToGL(float)} previously
     */
    public void addAll(ParticleCloud other) {
        this.bulk.addAll(other.bulk);
        this.maxTTL = Math.max(this.maxTTL, other.maxTTL);
        this.minTTL = Math.min(this.minTTL, other.minTTL);
    }

    /**
     * @return true if particles will be loaded to the GPU after a call to writeToGl()
     * This returns false if there are no particles loaded, or writeToGl has already been called
     */
    public boolean readyToLoad() {
        return (bulk != null) && !bulk.isEmpty();
    }

    private class Particle {
        public final PosVector[] sides; // positions relative to middle
        public final PosVector position;
        public final DirVector movement;
        public final Color4f color;
        public final AxisAngle4f rotation;
        public final float timeToLive;

        public Particle(PosVector A, PosVector B, PosVector C, DirVector movement, Color4f color, Vector3f angVec, float rotationSpeed, float timeToLive) {
            this.position = new PosVector();
            this.position.set(A).add(B).add(C).div(3);
            A.sub(position);
            B.sub(position);
            C.sub(position);
            this.sides = new PosVector[]{A, B, C};
            this.movement = movement;
            this.color = color;
            this.rotation = new AxisAngle4f(rotationSpeed, angVec);
            this.timeToLive = timeToLive;
        }

        public Particle(PosVector position, DirVector movement, Color4f color, Vector3f angVec, float rotationSpeed, float timeToLive, float particleSize) {
            this.position = position;
            this.movement = movement;
            this.color = color;
            this.rotation = new AxisAngle4f(rotationSpeed, angVec);
            this.timeToLive = timeToLive;

            // random positions
            PosVector A = Vector.random().scale(particleSize / 2).toPosVector();
            PosVector B = Vector.random().scale(particleSize / 2).toPosVector();
            PosVector C = A.add(B, new PosVector()).scale(-0.5f); // C = -1 * (A + B)/2
            this.sides = new PosVector[]{A, B, C};
        }
    }
}
