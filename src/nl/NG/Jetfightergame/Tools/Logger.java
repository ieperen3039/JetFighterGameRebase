package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Settings.ServerSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 2-6-2018.
 */
public class Logger {
    private static Consumer<String> out = System.out::println;
    private static List<Supplier<String>> onlinePrints = new CopyOnWriteArrayList<>();
    /** prevents spamming the chat */
    static Set<String> callerBlacklist = new HashSet<>();

    /**
     * prints the toString method of the given objects to System.out, preceded with calling method
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
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        do {
            caller = stackTrace[level++];
        } while (caller.isNativeMethod());

        System.err.println(caller + ": " + concatenate(o));
    }

    /**
     * prints the toString method of the given objects to the debug output, preceded with the method caller specified by
     * the given call depth
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
     * prints the toString method of the given objects to System.out, preceded with calling method. Every unique
     * callside will only be allowed to print once. For recursive calls, every level will be regarded as a new level,
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
    public static void setOutput(Consumer<String> newOutput) {
        if (newOutput == null) {
            printError("New output is null");
            return;
        }

        out = newOutput;
    }

    /**
     * adds a line to the online output roll
     * @param source
     */
    public static void printOnline(Supplier<String> source) {
        if (source == null) {
            printError("source is null");
        }
        onlinePrints.add(source);
    }

    /**
     * puts a message on the debug screen, which is updated every frame
     * @param accepter a method that prints the given string, on the same position as a previous call to this method
     */
    public static void setOnlineOutput(Consumer<String> accepter) {
        for (Supplier<String> source : onlinePrints) {
            String message = source.get();
            accepter.accept(message);
        }
    }

    /**
     * DEBUG method to get the calling method name
     * @param level the stack depth to receive. -1 = this method {@code getCallingMethod(int)} 0 = the calling method
     *              (yourself) 1 = the caller of the method this is called in
     * @return a string that completely describes the path to the file, the method and line number where this is called
     *         If DEBUG == false, return an empty string
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
     * removes the specified updater off the debug screen
     * @param source an per-frame updated debug message that has previously added to the debug screen
     */
    public static void removeOnlineUpdate(Supplier<String> source) {
        onlinePrints.remove(source);
    }
}
