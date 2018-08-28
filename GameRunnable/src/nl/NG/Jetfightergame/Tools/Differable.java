package nl.NG.Jetfightergame.Tools;

/**
 * Created by Geert van Ieperen on 16-5-2017.
 */
@FunctionalInterface
public interface Differable {
    /**
     * @return current() - previous()
     */
    Object difference();
}
