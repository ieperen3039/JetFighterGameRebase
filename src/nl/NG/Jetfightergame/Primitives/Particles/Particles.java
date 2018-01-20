package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A utility-class for particles
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public final class Particles {
    private static final float RANDOM_ROTATION = 1f;

    /**
     * generate particles for the given plane
     * @param plane the plane to generate particles of
     * @param ms a translation to map to world-space
     * @param entityPosition world-space position of the actual entity
     * @param launchSpeed speed in m/s of the resulting particles
     * @param planeColor color of this piece
     * @return a collection of particles that covers the plane exactly once
     */
    public static Collection<Particle> splitIntoParticles(Plane plane, MatrixStack ms, PosVector entityPosition, float launchSpeed, Color4f planeColor) {

        PosVector planeMiddle = ms.getPosition(plane.getMiddle());
        DirVector launchDir = entityPosition.to(planeMiddle, new DirVector());

        return splitIntoParticles(plane, 1, launchDir, 0.2f, 50, launchSpeed, planeColor, ms);
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
     * @param particleColor color of the particle
     */
    public static Particle generateParticle(PosVector a, PosVector b, PosVector c, DirVector movement, Vector3f angleVector, float rotationSpeed, float timeToLive, Color4f particleColor){
        PosVector centroid = a.add(b, new PosVector()).add(c, new PosVector()).scale(1/3f, new PosVector()).toPosVector(); // works when (A+B+C < Float.MAX_VALUE)
        DirVector A = centroid.to(a, new DirVector());
        DirVector B = centroid.to(b, new DirVector());
        DirVector C = centroid.to(c, new DirVector());
        return new TriangleParticle(A, B, C, centroid, movement, angleVector, rotationSpeed, timeToLive, particleColor);
    }

    /**
     * factory for a particle based on world-space.
     * the particle receives a random rotation
     * @param a one point in world-space
     * @param b another point in wold-space
     * @param c third point in world-space
     * @param movement direction in which this particle is moving (m/s)
     * @param timeToLive seconds before this particle should be destroyed
     * @param particleColor color of the particle
     */
    public static Particle generateParticle(PosVector a, PosVector b, PosVector c, DirVector movement, float timeToLive, Color4f particleColor){
        Vector3f angleVector = DirVector.random();
        float rotationSpeed = Settings.random.nextFloat();
        rotationSpeed *= rotationSpeed * RANDOM_ROTATION;
        return generateParticle(a, b, c, movement, angleVector, rotationSpeed, timeToLive, particleColor);
    }

    /**
     * creates particles to fill the target plane with particles
     * @param targetPlane the plane to be broken
     * @param splits the number of times this Plane is split into four. If this Plane is not a triangle,
     *               the resulting number of splits will be ((sidepoints - 3) * 2) as many
     * @param launchDir the direction these new particles should move to
     * @param jitter a factor that shows randomness in direction (divergence from launchDir).
     *               A jitter of 1 results in angles up to 45 degrees / (1/4pi) rads
     * @param deprecationTime maximum time that the resulting particles may live
     * @param particleSpeed average speed in m/s of the resulting particles
     * @param planeColor color of this plane
     * @param ms translation matrix to world-space
     * @return a set of particles that completely fills the plane, without overlap and in random directions
     */
    public static Collection<Particle> splitIntoParticles(
            Plane targetPlane, int splits, DirVector launchDir, float jitter,
            int deprecationTime, float particleSpeed, Color4f planeColor, MatrixStack ms
    ) {

        Collection<PosVector[]> triangles = asTriangles(targetPlane, ms);

        Collection<PosVector[]> splittedTriangles = triangulate(triangles, splits);

        return getParticles(splittedTriangles, launchDir, jitter, deprecationTime, particleSpeed, planeColor);
    }

    private static Collection<Particle> getParticles(Collection<PosVector[]> splittedTriangles, DirVector launchDir, float jitter,
                                                     int deprecationTime, float speed, Color4f particleColor) {
        Collection<Particle> particles = new ArrayList<>();
        for (PosVector[] p : splittedTriangles){
            DirVector movement = new DirVector();
            DirVector random = DirVector.random();

            launchDir.normalize(movement)
                    .add(random.scale(jitter, random), movement)
                    .scale(speed, movement);

            particles.add(generateParticle(
                    p[0], p[1], p[2], movement, Settings.random.nextFloat() * deprecationTime, particleColor)
            );
        }
        return particles;
    }

    /**
     * splits the given triangles in smaller triangles
     * @param splits number of iterations. the number of resulting triangles grows exponentially
     * @return triangles in the same definition as the input triangles
     */
    private static Collection<PosVector[]> triangulate(Collection<PosVector[]> triangles, int splits) {
        if (splits == 0) return triangles;

        Collection<PosVector[]> splittedTriangles = new ArrayList<>();
        for (int i = 0; i < splits; i++) {
            splittedTriangles.clear();
            triangles.forEach((p) -> splittedTriangles.addAll((splitTriangle(p[0], p[1], p[2]))));
            triangles = splittedTriangles;
        }
        return splittedTriangles;
    }

    /**
     * breaks the object up in triangles
     * @param ms translation matrix to world-space
     * @return collection of these triangles in world-space
     */
    private static Collection<PosVector[]> asTriangles(Plane targetPlane, MatrixStack ms) {
        Collection<PosVector[]> triangles = new ArrayList<>();
        Iterator<PosVector> border = targetPlane.getBorderAsStream().iterator();

        // split into triangles and add those
        PosVector A, B, C;
        try {
            A = ms.getPosition(border.next());
            B = ms.getPosition(border.next());
            C = ms.getPosition(border.next());
        } catch (NoSuchElementException ex) {
            // a plane without at least two edges can not be split
            throw new IllegalArgumentException("Plane with less than three vertices can not be split", ex);
        }

        triangles.add(new PosVector[]{A, B, C});

        while (border.hasNext()) {
            A = B;
            B = C;
            C = ms.getPosition(border.next());
            triangles.add(new PosVector[]{A, B, C});
        }
        return triangles;
    }

    /**
     * creates four particles splitting the triangle between the given coordinates like the triforce (Zelda)
     * @return Collection of four Particles
     */
    private static Collection<PosVector[]> splitTriangle(PosVector A, PosVector B, PosVector C){
        Collection<PosVector[]> particles = new ArrayList<>();

        final PosVector AtoB = A.middleTo(B, new PosVector());
        final PosVector AtoC = A.middleTo(C, new PosVector());
        final PosVector BtoC = B.middleTo(C, new PosVector());

        particles.add(new PosVector[]{A, AtoB, AtoC});
        particles.add(new PosVector[]{B, BtoC, AtoB});
        particles.add(new PosVector[]{C, BtoC, AtoC});
        particles.add(new PosVector[]{AtoB, AtoC, BtoC});

        return particles;
    }
}
