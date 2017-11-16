package nl.NG.Jetfightergame.Tools.Tracked;

import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

/**
 * @author Geert van Ieperen
 *         created on 12-11-2017.
 */
public class TrackedCoordinate extends TrackedDifferable<ScreenCoordinate> {
    public TrackedCoordinate(ScreenCoordinate current, ScreenCoordinate previous) {
        super(current, previous);
    }

    public TrackedCoordinate(ScreenCoordinate initial) {
        super(initial);
    }

    public TrackedCoordinate() {
        super(new ScreenCoordinate(0, 0));
    }

    @Override
    public ScreenCoordinate difference() {
        return current().subtract(previous().x, previous().y);
    }

    @Override
    public void addUpdate(ScreenCoordinate addition) {
        update(current().add(addition.x, addition.y));
    }
}
