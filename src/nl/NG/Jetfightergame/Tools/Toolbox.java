package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.AbstractEntities.MortalEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.ShapeFromFile;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import org.joml.Quaternionf;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Geert van Ieperen on 31-1-2017. a class with various tools
 */
public final class Toolbox {

    // universal random to be used everywhere
    public static final Random random = new Random();
    private static Consumer<String> out = System.out::println;
    private static List<Supplier<String>> onlinePrints = new ArrayList<>();

    /** prevents spamming the chat */
    private static Set<String> callerBlacklist = new HashSet<>();

    private static final float ROUNDINGERROR = 1E-6F;

    /**
     * prints the toString method of the given objects to System.out, preceded
     * with calling method
     */
    public static void print(Object... o) {
        printFrom(2, o);
    }

    /**
     * the system error version of {@link #print(Object...)}, but always print caller
     * @param o the objects to print
     */
    public static void printError(Object... o) {
        int level = 1;
        StackTraceElement caller;
        Exception ex = new Exception();
        do {
            caller = ex.getStackTrace()[level++];
        } while (caller.isNativeMethod());

        System.err.println(caller + ": " + concatenate(o));
    }

    /**
     * prints the toString method of the given objects to the debug output, preceded
     * with the method caller specified by the given call depth
     * @param level 0 = this method, 1 = the calling method (yourself)
     */
    public static void printFrom(int level, Object... o) {
        String source = getCallingMethod(level);
        write(source, o);
    }

    /** the actual writing function */
    private static synchronized void write(String source, Object[] o) {
        out.accept(source + ": " + concatenate(o));
    }

    private static String concatenate(Object[] x) {
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
     * prints the toString method of the given objects to System.out, preceded
     * with calling method. Every unique callside will only be allowed to print
     * once. For recursive calls, every level will be regarded as a new level,
     * thus print once for every unique depth
     * @param identifier
     * @param o
     */
    public static synchronized void printSpamless(String identifier, Object... o) {
        if (!callerBlacklist.contains(identifier)) {
            printFrom(2, o);
            callerBlacklist.add(identifier);
        }
    }

    /**
     * sets the debug output of the print methods to the specified file
     * @param newOutput
     */
    public static void setOutput(Consumer<String> newOutput){
        if (newOutput == null) {
            Toolbox.printError("New output is null");
            return;
        }

        out = newOutput;
    }

    /**
     * adds a line to the online output roll
     * @param source
     */
    public static void printOnline(Supplier<String> source){
        if (source == null) {
            Toolbox.printError("source is null");
        }
        onlinePrints.add(source);
    }

    /**
     * puts a message on the debug screen, which is updated every frame
     * @param accepter a method that prints the given string, on the same position as a previous call to this method
     */
    public static void setOnlineOutput(Consumer<String> accepter){
        for (Supplier<String> source : onlinePrints) {
            String message = source.get();
            accepter.accept(message);
        }
    }

    /**
     * DEBUG method to get the calling method name
     * @param level the stack depth to receive. -1 = this method {@code
     *              getCallingMethod(int)} 0 = the calling method (yourself) 1 =
     *              the caller of the method this is called in
     * @return a string that completely describes the path to the file, the
     *         method and line number where this is called If DEBUG == false,
     *         return an empty string
     */
    public static String getCallingMethod(int level) {
        if (!ServerSettings.DEBUG) return "";

        StackTraceElement caller;
        Exception exception = new Exception();
        do {
            caller = exception.getStackTrace()[++level]; // level + 1
        } while (caller.isNativeMethod());

        return caller.toString();
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public static void drawAxisFrame(GL2 gl) {
        if (!ServerSettings.DEBUG) return;

        String source = getCallingMethod(1);
        if (!callerBlacklist.contains(source)) {
            print(source, " - draws axis frame");
            callerBlacklist.add(source);
        }

        Material mat = Material.GLOWING;
        gl.pushMatrix();
        {
            gl.setMaterial(mat, Color4f.BLUE);
            gl.draw(ShapeFromFile.ARROW);
            gl.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            gl.setMaterial(mat, Color4f.RED);
            gl.draw(ShapeFromFile.ARROW);
            gl.rotate((float) Math.toRadians(-90), 1f, 0f, 0f);
            gl.setMaterial(mat, Color4f.GREEN);
            gl.draw(ShapeFromFile.ARROW);
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
            printFrom(2, "glError " + asHex(error) + ": " + glGetString(error));
        }
    }

    public static String asHex(int decimal) {
        return "0x" + Integer.toHexString(decimal).toUpperCase();
    }


    public static void checkALError() {
        if (!ServerSettings.DEBUG) return;
        int error;
        while ((error = alGetError()) != AL_NO_ERROR) {
            printFrom(2, "alError " + asHex(error) + ": " + alGetString(error));
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
            printError(": Tried to exit JVM while DEBUG mode is false.");
        }

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
     * merges a joining array into this array, and removes {@link MortalEntity}
     * entities that are overdue as in {@link MortalEntity#isDead()}
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

    /** @return a rotation that maps the x-vector to the given direction, with up in direction of z */
    public static Quaternionf xTo(DirVector direction) {
        return new Quaternionf().rotateTo(DirVector.xVector(), direction);
    }

    /**
     * removes the specified updater off the debug screen
     * @param source an per-frame updated debug message that has previously added to the debug screen
     */
    public static void removeOnlineUpdate(Supplier<String> source) {
        onlinePrints.remove(source);
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
