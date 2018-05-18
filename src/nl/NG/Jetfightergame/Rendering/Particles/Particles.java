package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * A utility-class for particles
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public final class Particles {
    private static final float RANDOM_ROTATION = 0.2f;
    private static final int FIRE_LINGER_TIME = 2;
    private static final int METAL_LINGER_TIME = 3;
    private static final int FIRE_PARTICLE_SPLITS = 2;

    /**
     * generate particles for the given plane
     * @param plane the plane to generate particles of
     * @param ms a translation to map to world-space
     * @param entityPosition world-space position of the actual entity
     * @param launchSpeed speed in m/s of the resulting particles
     * @param planeColor color of this piece
     * @param startVelocity
     * @return a collection of particles that covers the plane exactly once
     */
    public static ParticleCloud splitIntoParticles(Plane plane, MatrixStack ms, PosVector entityPosition, float launchSpeed, Color4f planeColor, Vector3fc startVelocity) {

        PosVector planeMiddle = ms.getPosition(plane.getMiddle());
        DirVector launchDir = entityPosition.to(planeMiddle, new DirVector());
        launchDir.add(startVelocity);

        final float jitter = 0.4f;
        return splitIntoParticles(plane, ClientSettings.PARTICLE_SPLITS, launchDir, jitter, METAL_LINGER_TIME, launchSpeed, planeColor, ms);
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
    public static ParticleCloud splitIntoParticles(
            Plane targetPlane, int splits, DirVector launchDir, float jitter,
            int deprecationTime, float particleSpeed, Color4f planeColor, MatrixStack ms
    ) {

        Collection<PosVector[]> triangles = asTriangles(targetPlane, ms);

        Collection<PosVector[]> splittedTriangles = triangulate(triangles, splits);

        return getParticles(splittedTriangles, launchDir, jitter, deprecationTime, particleSpeed, planeColor);
    }

    private static ParticleCloud getParticles(Collection<PosVector[]> splittedTriangles, DirVector launchDir, float jitter,
                                                     int deprecationTime, float speed, Color4f particleColor) {
        ParticleCloud particles = new ParticleCloud();
        for (PosVector[] p : splittedTriangles){
            DirVector movement = new DirVector();
            DirVector random = DirVector.random();

            float randFloat = ServerSettings.random.nextFloat();

            launchDir.add(random.scale(jitter, random), movement)
                    .scale(speed * (1 - randFloat), movement);

            particles.addParticle(
                    p[0], p[1], p[2], movement, random, particleColor, randFloat, randFloat * randFloat * randFloat * deprecationTime
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

        for (int i = 0; i < splits; i++) {
            triangles = triangles.stream()
                    .map(p -> splitTriangle(p[0], p[1], p[2]))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return triangles;
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

        final PosVector AtoB = A.middleTo(B);
        final PosVector AtoC = A.middleTo(C);
        final PosVector BtoC = B.middleTo(C);

        particles.add(new PosVector[]{A, AtoB, AtoC});
        particles.add(new PosVector[]{B, BtoC, AtoB});
        particles.add(new PosVector[]{C, BtoC, AtoC});
        particles.add(new PosVector[]{AtoB, AtoC, BtoC});

        return particles;
    }
}
