package nl.NG.Jetfightergame.Vectors;

/**
 * @author Geert van Ieperen
 *         created on 5-11-2017.
 */
public class ScreenCoordinate {
    public final int x, y;

    public ScreenCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ScreenCoordinate add(int x, int y){
        return new ScreenCoordinate(this.x + x, this.y + y);
    }

    public ScreenCoordinate subtract(int x, int y) {
        return new ScreenCoordinate(this.x - x, this.y - y);
    }
}
