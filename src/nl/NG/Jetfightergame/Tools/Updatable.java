package nl.NG.Jetfightergame.Tools;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
@FunctionalInterface
public interface Updatable {
    /**
     * updateGameLoop the internal structure of this object based on the given value
     * should always call super.updateGameLoop() if applicable
     * @param deltaTime time since last updateGameLoop
     *
     */
    void update(float deltaTime);
}
