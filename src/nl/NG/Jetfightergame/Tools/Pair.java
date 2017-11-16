package nl.NG.Jetfightergame.Tools;

/**
 * Pair class that simply holds two variables.
 *
 * @param <L> Left type
 * @param <R> Right type
 */
public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * creates a pair with [null, null]
     */
    public Pair() {
        left = null;
        right = null;
    }

    @Override
    public String toString() {
        return "[" + left + ", " + right + "]";
    }
}
