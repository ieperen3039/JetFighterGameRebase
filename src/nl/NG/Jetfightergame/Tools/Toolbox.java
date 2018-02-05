package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.ShapeCreators.ShapeFromMesh;
import nl.NG.Jetfightergame.Vectors.Color4f;
import org.joml.Vector4f;

import java.util.HashSet;
import java.util.Set;

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

    private static final float ROUNDINGERROR = 1E-6F;

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
            s.append(" | ").append(x[i]);
        }
        return s.toString();
    }

    /**
     * prints the toString method of the given objects to System.out, preceded with calling method.
     * Every unique callside will only be allowed to print once.
     * For recursive calls, every level will be regarded as a new level, thus print once for every unique depth
     * @param identifier
     * @param x
     */
    public static synchronized void printSpamless(String identifier, Object... x){
        if (!callerBlacklist.contains(identifier)) {
            System.out.println(Toolbox.getCallingMethod(1) + ": " + getValues(x));
            callerBlacklist.add(identifier);
        }
    }

    /**
     * DEBUG method to get the calling method name
     * @param level the stack depth to receive.
     *              -1 = this method {@code getCallingMethod(int)}
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
            gl.draw(ShapeFromMesh.ARROW);
            gl.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            gl.setMaterial(mat, Color4f.RED);
            gl.draw(ShapeFromMesh.ARROW);
            gl.rotate((float) Math.toRadians(-90), 1f, 0f, 0f);
            gl.setMaterial(mat, Color4f.GREEN);
            gl.draw(ShapeFromMesh.ARROW);
            gl.scale(0.2f);
            gl.setMaterial(Material.ROUGH, Color4f.WHITE);
            gl.draw(GeneralShapes.CUBE);
        }
        gl.popMatrix();
    }

    public static void checkGLError(){
        if (!Settings.DEBUG) return;
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            printFrom(2, ": glError " + error);
        }
    }

    /**
     * call System.exit and tells who did it
     */
    public static void exitJava() {
        if (!Settings.DEBUG) return;
        try {
            System.out.println();
            Toolbox.printFrom(2, "Ending JVM");
            Thread.sleep(10);
            System.err.println();
        } finally {
            throw new RuntimeException();
        }
    }

    public static boolean almostZero(float number) {
        return (((number + ROUNDINGERROR) >= 0.0f) && ((number - ROUNDINGERROR) <= 0.0f));
    }

    /**
     * runs the specified action after the given delay.
     * This may be cancelled, upon which this thread will not do anything after the delay ends.
     */
    public static class DelayedAction extends Thread {
        private final long delay;
        private boolean cancelled = false;

        public DelayedAction(long delay, Runnable action) {
            super(action);
            this.delay = delay;
            this.setDaemon(true);
            start();
        }

        public void cancel(){
            cancelled = true;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
                if (!cancelled) super.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
