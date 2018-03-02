package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
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
     * we find that to map Rc to Tc, we cannot use multiplication, but instead we must translate with Tc,
     * giving us a mapping M(p) = A*p*Tc for any point in R for some transformation matrix A.
     * 6 indices for A are found by solving [A*Ra + Tc = Ta & A*Rb + Tc = Tb] for A, but not all.
     * The remaining indices describe transformations for when the base triangle has values in the n-th dimension
     * (in our case, only the 3rd dimension). Yet our triangle does not have any values in the n-th dimension,
     * so these should be 0.
     * We pre-apply our transformation with Rc to get our new A.
     *
     * @return transformation matrix that maps R [(1, 0, 0), (0, 1, 0), (0, 0, 0)] to a triangle with the given vertices.
     */
    protected static Matrix4f getMapping(Vector3f a, Vector3f b, Vector3f c){
        // transpose because of the definition of Matrix4f constructor
        return new Matrix4f(
                a.x - c.x, b.x - c.x, 0.00000001f, c.x,
                a.y - c.y, b.y - c.y, 0.00000001f, c.y,
                a.z - c.z, b.z - c.z, 0.00000001f, c.z, // must think of something to make 'det != 0'
                0, 0, 0, 1
        ).transpose().assumeAffine();
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
    public void updateRender(float deltaTime) {
        currentRotation += rotationSpeed * deltaTime;
        x += movement.x() * deltaTime;
        y += movement.y() * deltaTime;
        z += movement.z() * deltaTime;
        timeToLive -= deltaTime;
    }

    @Override
    public boolean isOverdue() {
        return timeToLive <= 0;
    }
}
