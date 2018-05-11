package nl.NG.Jetfightergame;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public final class Identity {
    private static int next = 0;
    private static boolean active = true;

    public static synchronized int next(){
        if (active) return next++;
        else throw new UnsupportedOperationException("Identity generation was explicitly disabled");
    }

    public static void disable(){
        active = false;
    }
}
