package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen
 *         created on 1-11-2017.
 */
public class TriangleParticle implements Particle {

    public static final int RANDOM_TTL = 5;
    /** the three points of this relative to position */
    private final DirVector movement;
    private final Vector3f angVec;
    private final float rotationSpeed;
    /** position of this */
    private float x, y, z;
    private float timeToLive;
    private float currentRotation = 0;

    /** to map the general triangle to a unit triangle */

    private final Matrix4f combinedTransformation;
    private Color4f color;

    /**
     * creates a triangle particle in world-space
     * @param a one point relative to pos
     * @param b another point relative to pos
     * @param c third point relative to pos
     * @param centroid position of the middle of the object
     * @param movement direction in which this particle is moving (m/s)
     * @param angleVector vector orthogonal on the rotationSpeed of this particle
     * @param rotationSpeed rotation speed of this particle (rad/s)
     * @param timeToLive seconds before this particle should be destroyed
     * @param particleColor absolute color of this particle
     */
    public TriangleParticle(Vector a, Vector b, Vector c, PosVector centroid,
                            DirVector movement, Vector3f angleVector, float rotationSpeed, float timeToLive, Color4f particleColor) {
        x = centroid.x();
        y = centroid.y();
        z = centroid.z();
        color = particleColor;

        combinedTransformation = getMapping(a, b, c);

        this.movement = movement;
        this.angVec = angleVector;
        this.rotationSpeed = rotationSpeed;
        this.timeToLive = timeToLive;
    }

    /**
     * creates a matrix that transforms the unit triangle (u, v, O) to an arbitrary triangle (a, b, c)
     *
     * Proof of Correctness: Mapping from unit triangle to an arbitrary triangle in n-dimensional space.
     * 17-11-2017 / Geert van Ieperen / source: http://people.sc.fsu.edu/~jburkardt/presentations/cg_lab_mapping_triangles.pdf
     *
     * Problem:
     * we have a standard triangle R [a(1, 0, 0), b(0, 1, 0), c(0, 0, 0)], possibly in a larger dimension.
     * map this triangle to an arbitrary triangle T with vertices A, B and C using matrix transformations
     *
     * Solution:
     * we find that to map Rc to Tc, we cannot use multiplication, but instead we must translate with Rc,
     * giving us a mapping M(p) = A*p*Rc for any point in R for some transformation matrix A.
     * some indices for A are found by calculating M(p) for p = Ra and Rb, but not all.
     * The remaining indices describe transformations for when the base triangle has values in the n-th dimension
     * (in our case, only the 3rd dimension). Yet our triangle does not have any values in the 3rd dimension,
     * so we are fine with 0's.
     * Multiplying C with our new A gives R.
     */
    protected static Matrix4f getMapping(Vector3f a, Vector3f b, Vector3f c){
        // apply transformation M(p)
        Matrix4f result = new Matrix4f(
                a.x - c.x, b.x - c.x, 0, 0,
                a.y - c.y, b.y - c.y, 0, 0,
                a.z - c.y, b.z - c.z, 0, 0,
                0, 0, 0, 1
        ).transpose();
        // translate with point c
        result.translate(c);
        return result;
    }

    public void draw(GL2 gl) {
        gl.setMaterial(Material.GLOWING, color);
        gl.pushMatrix();
        {
            gl.translate(x, y, z);
            gl.rotate(currentRotation, angVec.x(), angVec.y(), angVec.z());
            gl.multiplyAffine(combinedTransformation);
            gl.draw(GeneralShapes.TRIANGLE);
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

    @Override
    public void render(GL2.Painter lock) {

    }
}
