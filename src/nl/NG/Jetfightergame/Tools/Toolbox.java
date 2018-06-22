package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import org.joml.Quaternionf;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Geert van Ieperen on 31-1-2017. a class with various tools
 */
public final class Toolbox {

    // universal random to be used everywhere
    public static final Random random = new Random();

    private static final float ROUNDINGERROR = 1E-6F;

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public static void drawAxisFrame(GL2 gl) {
        if (!ServerSettings.DEBUG) return;

        String source = Logger.getCallingMethod(1);
        if (!Logger.callerBlacklist.contains(source)) {
            Logger.print(source, " - draws axis frame");
            Logger.callerBlacklist.add(source);
        }

        Material mat = Material.GLOWING;
        gl.pushMatrix();
        {
            gl.setMaterial(mat, Color4f.BLUE);
            gl.draw(GeneralShapes.ARROW);
            gl.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            gl.setMaterial(mat, Color4f.RED);
            gl.draw(GeneralShapes.ARROW);
            gl.rotate((float) Math.toRadians(-90), 1f, 0f, 0f);
            gl.setMaterial(mat, Color4f.GREEN);
            gl.draw(GeneralShapes.ARROW);
            gl.scale(0.2f);
            gl.setMaterial(Material.ROUGH, Color4f.WHITE);
            gl.draw(GeneralShapes.CUBE);
        }
        gl.popMatrix();
    }

    public static void checkGLError() {
        if (!ServerSettings.DEBUG) return;
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            Logger.printFrom(2, "glError " + asHex(error) + ": " + glGetString(error));
        }
    }

    public static String asHex(int decimal) {
        return "0x" + Integer.toHexString(decimal).toUpperCase();
    }


    public static void checkALError() {
        if (!ServerSettings.DEBUG) return;
        int error;
        while ((error = alGetError()) != AL_NO_ERROR) {
            Logger.printFrom(2, "alError " + asHex(error) + ": " + alGetString(error));
            if (error == AL_INVALID_OPERATION)
                break; // check for when method is called outside the AL context
        }
    }

    /**
     * call System.exit and tells who did it, unless DEBUG is false
     */
    public static void exitJava() {
        if (!ServerSettings.DEBUG) {
            final StackTraceElement caller = new Exception().getStackTrace()[1];
            Logger.printError(": Tried to exit JVM while DEBUG mode is false.");
        }

        try {
            System.out.println();
            Logger.printFrom(2, "Ending JVM");
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
     * performs an incremental insertion-sort on (preferably nearly-sorted)
     * array entities
     * @param items the array to sort
     * @param map   maps a moving source to the value to be sorted upon
     * @modifies items
     */
    public static <Type> void insertionSort(Type[] items, Function<Type, Float> map) {
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
                } else break;
            }
            items[empty] = subject;
        }
    }

    /**
     * merges a joining array into this array, and removes {@link TemporalEntity}
     * entities that are overdue as in {@link TemporalEntity#isOverdue()}
     * @param host the sorted largest of the arrays to merge, entities in this
     *             array will be checked for relevance.
     * @param join the sorted other array to merge
     * @param map  maps a moving source to the value to be sorted upon
     * @return a sorted array of living entities from both host and join
     *         combined.
     */
    public static <Type> Type[] mergeAndClean(Type[] host, Type[] join, Function<Type, Float> map) {
        Type[] results = Arrays.copyOf(host, host.length + join.length);

        int hIndex = 0;
        int jIndex = 0;

        // we let the final number of iterations i be available after the loop ends
        int i = 0;

        // while loop, so i++ indexing is required
        while (i < results.length) {
            // all host items must be checked for isDead, so first see if there are any left
            if (hIndex >= host.length) {
                results[i++] = join[jIndex++];

            } else {
                Type hostItem = host[hIndex];

                // check whether it is alive
                if (TemporalEntity.isOverdue(hostItem)) {
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

    /** @return a rotation that maps the x-vector to the given direction, with up in direction of z */
    public static Quaternionf xTo(DirVector direction) {
        return new Quaternionf().rotateTo(DirVector.xVector(), direction);
    }

    /** returns a  */
    public static float randomBetween(float val1, float val2) {
        return val1 + ((val2 - val1) * random.nextFloat());
    }

    /**
     * runs the specified action after the given delay. This may be cancelled,
     * upon which this thread will not do anything after the delay ends.
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

        public void cancel() {
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
