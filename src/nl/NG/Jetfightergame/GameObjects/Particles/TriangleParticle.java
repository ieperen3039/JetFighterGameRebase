package nl.NG.Jetfightergame.GameObjects.Particles;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 1-11-2017.
 */
public class TriangleParticle implements AbstractParticle {

    public static final int RANDOM_TTL = 5;
    private static final float RANDOM_ROTATION = 1f;
    /** the three points of this relative to position */
    private final DirVector A, B, C;
    private final DirVector movement;
    private final DirVector angVec;
    private final float rotationSpeed;
    /** position of this */
    private float x, y, z;
    private float timeToLive;
    private float currentRotation = 0;

    /**
     * creates a triangle particle in world-space
     * @param a one point relative to pos
     * @param b another point relative to pos
     * @param c third point relative to pos
     * @param position pos of the middle of the object
     * @param movement direction in which this particle is moving (m/s)
     * @param angleVector vector orthogonal on the rotationSpeed of this particle
     * @param rotationSpeed rotation speed of this particle (rad/s)
     * @param timeToLive seconds before this particle should be destroyed
     */
    private TriangleParticle(DirVector a, DirVector b, DirVector c, PosVector position,
                             DirVector movement, DirVector angleVector, float rotationSpeed, float timeToLive) {
        A = a;
        B = b;
        C = c;
        x = (float) position.x();
        y = (float) position.y();
        z = (float) position.z();
        this.movement = movement;
        this.angVec = angleVector;
        this.rotationSpeed = rotationSpeed;
        this.timeToLive = timeToLive;
    }

    /**
     * factory for a particle based on world-space
     * @param a one point in world-space
     * @param b another point in wold-space
     * @param c third point in world-space
     * @param movement direction in which this particle is moving (m/s)
     * @param angleVector vector orthogonal on the rotationSpeed of this particle
     * @param rotationSpeed rotation speed of this particle (rad/s)
     * @param timeToLive seconds before this particle should be destroyed
     */
    public static TriangleParticle worldspaceParticle(PosVector a, PosVector b, PosVector c, DirVector movement, DirVector angleVector, float rotationSpeed, float timeToLive){
        PosVector position = a.add(b).add(c).scale(1.0/3).toPosVector(); // works when (A+B+C < Double.MAX_VALUE)
        DirVector A = position.to(a);
        DirVector B = position.to(b);
        DirVector C = position.to(c);
        return new TriangleParticle(A, B, C, position, movement, angleVector, rotationSpeed, timeToLive);
    }

    /**
     * factory for a particle based on world-space.
     * the particle receives a random rotation
     * @param a one point in world-space
     * @param b another point in wold-space
     * @param c third point in world-space
     * @param movement direction in which this particle is moving (m/s)
     * @param timeToLive seconds before this particle should be destroyed
     */
    public static TriangleParticle worldspaceParticle(PosVector a, PosVector b, PosVector c, DirVector movement, float timeToLive){
        DirVector angleVector = new DirVector(Settings.random.nextDouble() - 0.5, Settings.random.nextDouble() - 0.5, Settings.random.nextDouble() - 0.5);
        float rotationSpeed = Settings.random.nextFloat();
        rotationSpeed *= rotationSpeed * RANDOM_ROTATION;
        return worldspaceParticle(a, b, c, movement, angleVector, rotationSpeed, timeToLive);
    }

    @Override
    public void drawRaw(GL2 gl) {
        gl.pushMatrix();
        {
            gl.translate((double) x, (double) y, (double) z);
            gl.rotate((double) currentRotation, angVec.x(), angVec.y(), angVec.z());
            gl.vertex(A);
            gl.vertex(B);
            gl.vertex(C);
        }
        gl.popMatrix();
    }

    @Override
    public void updateRender(float time) {
        currentRotation += rotationSpeed * time;
        x += movement.x() * time;
        y += movement.y() * time;
        z += movement.z() * time;
        timeToLive -= time;
    }

    @Override
    public boolean alive() {
        return timeToLive > 0;
    }
}
