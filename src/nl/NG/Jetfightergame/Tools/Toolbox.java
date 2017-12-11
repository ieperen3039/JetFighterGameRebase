package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Primitives.Particles.TriangleParticle;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Vector4f;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

/**
 * Created by Geert van Ieperen on 31-1-2017.
 * a class with various tools
 */
public class Toolbox {

    public static final Vector4f COLOR_WHITE = new Vector4f(1, 1, 1, 1);
    public static final Vector4f COLOR_BLACK = new Vector4f(0, 0, 0, 1);
    public static final Vector4f COLOR_RED = new Vector4f(1, 0, 0, 1);
    public static final Vector4f COLOR_GREEN = new Vector4f(0, 1, 0, 1);
    public static final Vector4f COLOR_BLUE = new Vector4f(0, 0, 1, 1);

    /** prevents spamming the chat */
    private static Set<String> callerBlacklist = new HashSet<>();

    /**
     * prints the toString method of the given objects to System.out, preceded with calling method
     */
    public static synchronized void print(Object... x) {
        printFrom(2, x);
    }

    /**
     * prints the toString method of the given objects to System.out, preceded with the method
     * caller specified by the given call depth
     * @param level 0 = this method, 1 = the calling method (yourself)
     */
    public static synchronized void printFrom(int level, Object... x) {
        String source = getCallingMethod(level);
        System.out.println(source + ": " + getValues(x));
    }

    private static String getValues(Object[] x) {
        if (x.length == 0) return "";
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

    /**
     * prints the toString method of the given objects to System.out, preceded with calling method.
     * Every unique callside will only be allowed to print once.
     * For recursive calls, every level will be regarded as a new level, thus print once for every unique depth
     * @param x
     */
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
     * If DEBUG == false, return an empty string
     */
    public static String getCallingMethod(int level) {
        if (!Settings.DEBUG) return "";

        final StackTraceElement caller = new Exception().getStackTrace()[level + 1];
        return caller.getClassName() + "." + caller.getMethodName() + "(line:" + caller.getLineNumber() + ")";
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin (yellow).
     */
    public static void drawAxisFrame(GL2 gl) {
        if (!Settings.DEBUG) return;

        String source = getCallingMethod(1);
        if (!callerBlacklist.contains(source)) {
            System.out.println(source + " - draws axis frame");
            callerBlacklist.add(source);
        }

        Material mat = Material.GLOWING;
        gl.pushMatrix();
        {
            gl.setMaterial(mat, Color4f.BLUE);
            gl.draw(ShapeFromMesh.Arrow);
            gl.rotate(Math.toRadians(90), 0, 1, 0);
            gl.setMaterial(mat, Color4f.RED);
            gl.draw(ShapeFromMesh.Arrow);
            gl.rotate(Math.toRadians(-90), 1, 0, 0);
            gl.setMaterial(mat, Color4f.GREEN);
            gl.draw(ShapeFromMesh.Arrow);
            gl.scale(0.3f);
            gl.setMaterial(Material.SILVER, Color4f.WHITE);
            gl.draw(GeneralShapes.CUBE);
        }
        gl.popMatrix();
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
     * @param deprecationTime
     * @return a set of particles that completely fills the plane, without overlap and in random directions
     */
    public static Collection<AbstractParticle> splitIntoParticles(
            Plane targetPlane, PosVector worldPosition, int splits, DirVector launchDir, float jitter, int deprecationTime) {

        Collection<PosVector[]> triangles = new ArrayList<>();
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

        Collection<PosVector[]> splittedTriangles = new ArrayList<>();
        for (int i = 0; i < splits; i++) {
            triangles.forEach((p) -> splittedTriangles.addAll((splitTriangle(p[0], p[1], p[2]))));
            triangles = splittedTriangles;
        }

        Collection<AbstractParticle> particles = new ArrayList<>();

        for (PosVector[] p : splittedTriangles){
            DirVector movement = launchDir.normalized().add(DirVector.random().scale(jitter));
            particles.add(TriangleParticle.worldspaceParticle(
                    p[0], p[1], p[2], movement, Settings.random.nextFloat() * deprecationTime)
            );
        }

        return particles;
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

    public static void checkGLError(){
        if (!Settings.DEBUG) return;
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            printFrom(2, ": glError " + error);
        }
    }
}
