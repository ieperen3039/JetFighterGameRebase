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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (left != null ? left.equals(pair.left) : pair.left == null)
            if (right != null ? right.equals(pair.right) : pair.right == null) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" + left + ", " + right + "]";
    }
}
