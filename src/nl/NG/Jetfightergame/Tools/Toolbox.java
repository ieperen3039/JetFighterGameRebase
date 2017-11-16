package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.GameObjects.Particles.AbstractParticle;
import nl.NG.Jetfightergame.GameObjects.Particles.TriangleParticle;
import nl.NG.Jetfightergame.GameObjects.Structures.Mesh;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Plane;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Triangle;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Geert van Ieperen on 31-1-2017.
 * a class with various tools
 */
public class Toolbox {

    private static Cursor invisibleCursor;
    /** prevents spamming the chat */
    private static Set<String> callerBlacklist = new HashSet<>();

    /**
     * prints debug information with calling method (a debugging method)
     */
    public static synchronized void print(Object... x) {
        printFrom(2, x);
    }

    /**
     * prints debug information with calling method (a debugging method)
     */
    public static synchronized void printFrom(int level, Object... x) {
        if (!Settings.DEBUG) return;

        String source = getCallingMethod(level);
        System.out.println(source + ": " + getValues(x));
    }

    private static String getValues(Object[] x) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] == null)
                x[i] = "null";
        }
        StringBuilder s = new StringBuilder(x[0].toString());
        for (int i = 1; i < x.length; i++) {
            s.append(", ").append(x[i]);
        }
        return s.toString();
    }

    public static synchronized void printSpamless(Object ... x){
        String source = getCallingMethod(1);
        if (!callerBlacklist.contains(source)) {
            System.out.println(source + ": " + getValues(x));
            callerBlacklist.add(source);
        }
    }

    /**
     * DEBUG method to get the calling method name
     * @param level the stack depth to receive.
     *              -1 = this method
     *              0 = the calling method (yourself)
     *              1 = the caller of the method this is called in
     * @return a string that completely describes the path to the file, the method and line number where this is called
     */
    public static String getCallingMethod(int level) {
        final StackTraceElement caller = new Exception().getStackTrace()[level + 1];
        return caller.getClassName() + "." + caller.getMethodName() + "(line:" + caller.getLineNumber() + ")";
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin (yellow).
     * setColor is (red , green, blue)
     */
    public static void drawAxisFrame(GL2 gl) {
        if (!Settings.DEBUG) return;

        String source = getCallingMethod(1);
        if (!callerBlacklist.contains(source)) {
            System.out.println(source + " - draws axis frame");
            callerBlacklist.add(source);
        }

        gl.setMaterial(Material.ROUGH);

        gl.pushMatrix();
        {
            gl.setColor(0, 0, 1d);
            Mesh.ARROW.draw(gl);
            gl.rotate((double) 90, (double) 0, (double) 1, (double) 0);
            gl.setColor(1d, 0, 0);
            Mesh.ARROW.draw(gl);
            gl.rotate((double) -90, (double) 1, (double) 0, (double) 0);
            gl.setColor(0, 1d, 0);
            Mesh.ARROW.draw(gl);
        }
        gl.popMatrix();

        gl.clearColor();
    }

    /**
     * creates particles to setFill the target plane with particles
     * @param targetPlane the plane to be broken
     * @param worldPosition offset of the plane coordinates in world-space
     * @param splits the number of times this Plane is split into four. If this Plane is not a triangle,
     *               the resulting number of splits will be ((sidepoints - 3) * 2) as many
     * @param launchDir the direction these new particles should move to
     * @param jitter a factor that shows randomness in direction (divergence from launchDir).
     *               A jitter of 1 results in angles up to 45 degrees / (1/4pi) rads
     * @return a set of particles that completely fills the plane, without overlap and in random directions
     */
    public static Collection<AbstractParticle> splitIntoParticles(Plane targetPlane, PosVector worldPosition,
                                                                  int splits, DirVector launchDir, float jitter) {

        Collection<PosVector[]> triangles = new LinkedList<>();
        Iterator<PosVector> border = targetPlane.getVertices().iterator();

        if (targetPlane instanceof Triangle) {
            triangles.add(new PosVector[]{border.next(), border.next(), border.next()});
        } else {
            // split into triangles and add those
            /* all are int world-position */
            PosVector A, B, C;
            // a plane without at least two edges can not be split
            try {
                A = border.next().add(worldPosition);
                B = border.next().add(worldPosition);
                C = border.next().add(worldPosition);
            } catch (NoSuchElementException ex) {
                throw new IllegalArgumentException("Plane with less than three vertices can not be split", ex);
            }

            triangles.add(new PosVector[]{A, B, C});

            while (border.hasNext()) {
                A = B;
                B = C;
                C = border.next().add(worldPosition);
                triangles.add(new PosVector[]{A, B, C});
            }
        }

        Collection<PosVector[]> splittedTriangles = new LinkedList<>();
        for (int i = 0; i < splits; i++) {
            triangles.forEach((p) -> splittedTriangles.addAll((splitTriangle(p[0], p[1], p[2]))));
            triangles = splittedTriangles;
        }

        Collection<AbstractParticle> particles = new LinkedList<>();

        for (PosVector[] p : splittedTriangles){
            DirVector movement = launchDir.normalized().add(DirVector.random().scale(jitter));
            particles.add(TriangleParticle.worldspaceParticle(
                    p[0], p[1], p[2], movement, Settings.random.nextFloat() * TriangleParticle.RANDOM_TTL)
            );
        }

        return particles;
    }

    /**
     * creates four particles splitting the triangle between the given coordinates like the triforce (Zelda)
     * @return Collection of four Particles
     */
    private static Collection<PosVector[]> splitTriangle(PosVector A, PosVector B, PosVector C){
        Collection<PosVector[]> particles = new LinkedList<>();

        final PosVector AtoB = A.middleTo(B);
        final PosVector AtoC = A.middleTo(C);
        final PosVector BtoC = B.middleTo(C);

        particles.add(new PosVector[]{A, AtoB, AtoC});
        particles.add(new PosVector[]{B, BtoC, AtoB});
        particles.add(new PosVector[]{C, BtoC, AtoC});
        particles.add(new PosVector[]{AtoB, AtoC, BtoC});

        return particles;
    }

    public static Cursor getInvisibleCursor() {
        if (invisibleCursor == null){
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            invisibleCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    cursorImg, new Point(0, 0), "blank cursor");
        }
        return invisibleCursor;
    }

    public static Vector3f colorVector(Color c) {
        return new Vector3f(c.getRed(), c.getGreen(), c.getBlue());
    }
}
