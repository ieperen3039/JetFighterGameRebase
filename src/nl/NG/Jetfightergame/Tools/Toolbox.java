package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromMesh;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Geert van Ieperen on 31-1-2017.
 * a class with various tools
 */
public final class Toolbox {

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
    public static synchronized void print(Object... o) {
        printFrom(2, o);
    }

    /**
     * prints the toString method of the given objects to System.out, preceded with the method
     * caller specified by the given call depth
     * @param level 0 = this method, 1 = the calling method (yourself)
     */
    public static synchronized void printFrom(int level, Object... o) {
        String source = getCallingMethod(level);
        System.out.println(source + ": " + getValues(o));
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
     * @param o
     */
    public static synchronized void printSpamless(String identifier, Object... o){
        if (!callerBlacklist.contains(identifier)) {
            System.out.println(Toolbox.getCallingMethod(1) + ": " + getValues(o));
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
            printFrom(2, "glError " + asHex(error) + ": " + glGetString(error));
        }
    }

    public static String asHex(int error) {
        return "0x" + Integer.toHexString(error).toUpperCase();
    }


    public static void checkALError() {
        if (!Settings.DEBUG) return;
        int error;
        while ((error = alGetError()) != AL_NO_ERROR) {
            printFrom(2, "alError " + asHex(error) + ": " + alGetString(error));
            if (error == AL_INVALID_OPERATION) break; // check for when method is called outside the AL context
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
            new Exception().printStackTrace();
            System.exit(1);
        } finally {
            throw new Error();
        }
    }

    public static boolean almostZero(float number) {
        return (((number + ROUNDINGERROR) >= 0.0f) && ((number - ROUNDINGERROR) <= 0.0f));
    }

    /**
     * performs an incremental insertion-sort on (preferably nearly-sorted) array entities
     * @param items the array to sort
     * @param map maps a moving source to the value to be sorted upon
     * @modifies items
     */
    public static <Type> void insertionSort(Type[] items, Function<Type, Float> map){
        // iterate incrementally over the array
        for (int head = 1; head < items.length; head++) {
            Type subject = items[head];

            // decrement for the right position
            int empty = head;

            while (empty > 0) {
                Type target = items[empty - 1];

                if (map.apply(target) > map.apply(subject)) {
                    items[empty] = target;
                    empty--;
                }
                else break;
            }
            items[empty] = subject;
        }
    }

    /**
     * merges a joining array into this array, and removes entities that are overdue
     * @param host the sorted largest of the arrays to merge, entities in this array will be checked for relevance.
     * @param join the sorted other array to merge
     * @param map maps a moving source to the value to be sorted upon
     * @return a sorted array of living entities from both host and join combined.
     */
    public static <Type> Type[] mergeAndClean(Type[] host, Type[] join, Function<Type, Float> map){
        Type[] results = Arrays.copyOf(host, host.length + join.length);

        int hIndex = 0;
        int jIndex = 0;

        // we let the final number of iterations i be available after the loop ends
        int i = 0;

        // while loop, so i++ indexing is required
        while (i < results.length) {
            // all host items must be checked for isDead, so first see if there are any left
            if (hIndex >= host.length){
                results[i++] = join[jIndex++];

            } else {
                Type hostItem = host[hIndex];

                // check whether it is alive
                if ((hostItem instanceof MortalEntity) && ((MortalEntity) hostItem).isDead()) {
                    // skip adding this source to the resulting array, effectively deleting it.
                    hIndex++;

                } else if (jIndex >= join.length) {
                    results[i++] = hostItem;
                    hIndex++;

                } else {
                    Type joinItem = join[jIndex];

                    // select the smallest
                    if (map.apply(hostItem) < map.apply(joinItem)) {
                        results[i++] = hostItem;
                        hIndex++;

                    } else {
                        results[i++] = joinItem;
                        jIndex++;
                    }
                }
            }
        }

        // loop automatically ends after at most (i = alpha.length + beta.length) iterations
        return Arrays.copyOf(results, i);
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
