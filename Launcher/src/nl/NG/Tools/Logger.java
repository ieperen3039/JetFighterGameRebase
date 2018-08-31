package nl.NG.Tools;

import java.io.PrintStream;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 2-6-2018.
 */
@SuppressWarnings("Duplicates")
public enum Logger {
    DEBUG(System.out), INFO(System.out), WARN(System.err), ERROR(System.err);

    private Consumer<String> out;

    private boolean enabled = true;
    private String codeName = String.format("[%-5s]", this);

    Logger(PrintStream out) {
        this.out = out::println;
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
     * sets the debug output of the given print method to the specified output. If both regular and error is null, reset
     * to the default outputs
     * @param regular the new output
     * @param error   the error output
     */
    public static void setOutputReceiver(Consumer<String> regular, Consumer<String> error) {
        DEBUG.out = regular;
        INFO.out = regular;
        WARN.out = error;
        ERROR.out = error;
    }

    /**
     * sets the output of the given level to the receiver
     * @param receiver the new output
     */
    public void setOutputReceiver(Consumer<String> receiver) {
        out = receiver;
    }

    /**
     * adds another receiver to the logger of this level
     * @param receiver the new output
     */
    public void addOutputReceiver(Consumer<String> receiver) {
        out = out.andThen(receiver);
    }

    /**
     * DEBUG method to get the calling method name
     * @param level the stack depth to receive. -1 = this method {@code getCallingMethod(int)} 0 = the calling method
     *              (yourself) 1 = the caller of the method this is called in
     * @return a string that completely describes the path to the file, the method and line number where this is called
     *         If DEBUG == false, return an empty string
     */
    public static String getCallingMethod(int level) {
        StackTraceElement caller;
        Exception exception = new Exception();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        level++;
        do {
            caller = stackTrace[level++]; // level + 1
        } while (caller.isNativeMethod() && level < stackTrace.length);

        return String.format("%-100s ", caller);
    }

    public static void setLoggingLevel(Logger minimum) {
        Logger[] levels = values();
        for (int i = 0; i < levels.length; i++) {
            levels[i].enabled = (i >= minimum.ordinal());
        }
    }

    public static Logger getLoggingLevel() {
        Logger[] values = values();
        for (Logger logger : values) {
            if (logger.enabled) return logger;
        }
        return null; // no logging is enabled
    }

    public static void printRaw(String line) {
        DEBUG.out.accept(line);
    }

    public static void errorRaw(String line) {
        ERROR.out.accept(line);
    }

    /**
     * prints the toString method of the given objects to the debug output, preceded with the method caller specified by
     * the given call depth
     */
    private synchronized void printSync(Object... s) {
        out.accept(codeName + ": " + concatenate(s));
    }

    /**
     * prints the toString method of the given objects to System.out, preceded with calling method
     */
    public void print(Object... s) {
        if (!enabled) return;
        if (s.length > 0 && s[0] instanceof Exception) {
            printException((Exception) s[0]);
        } else {
            printSync(s);
        }
    }

    private void printException(Exception e) {
        StringBuilder stacktrace = new StringBuilder();
        stacktrace.append(e);
        if (this == ERROR) {
            for (StackTraceElement elt : e.getStackTrace()) {
                stacktrace.append("\n\t").append(elt);
            }
        }
        printSync(stacktrace.toString());
    }

    public void printf(String format, Object... arguments) {
        printSync(String.format(Locale.US, format, arguments));
    }

    public void newLine() {
        if (enabled) out.accept("");
    }

    public PrintStream getPrintStream() {
        return System.err;
    }
}
