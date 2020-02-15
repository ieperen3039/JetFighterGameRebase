package nl.NG.Jetfightergame.Tools.Tracked;

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
