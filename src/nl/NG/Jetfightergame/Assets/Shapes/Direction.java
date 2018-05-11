package nl.NG.Jetfightergame.Assets.Shapes;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Geert van Ieperen on 6-3-2017.
 * specifies a hierarchical direction
 */

public enum Direction {
    UP(),
    RIGHT(),
    DOWN(),
    LEFT(),
    FORWARD(),
    BACK(),

    UPLEFT(UP, LEFT),
    UPRIGHT(UP, RIGHT),
    UPFORWARD(UP, FORWARD),
    UPBACK(UP, BACK),

    DOWNLEFT(DOWN, LEFT),
    DOWNRIGHT(DOWN, RIGHT),
    DOWNFORWARD(DOWN, FORWARD),
    DOWNBACKWARD(DOWN, BACK),

    NONE();

    private final Set<Direction> directions = new HashSet<>();

    Direction() {
        directions.add(this);
    }

    Direction(Direction... directions) {
        Collections.addAll(this.directions, directions);
    }

    /**
     * @returns whether {@code that} is a subset of {@code this}
     */
    public boolean is(Direction that) {
        return directions.containsAll(that.directions);
    }

    /**
     * creates a displacement vector assuming that x is forward, z is up and y is left
     *
     * @param displacement the magnitude of the displacement
     * @return a vector to the displacement
     */
    public DirVector vector(double displacement) {
        // 3d displacements
        double x = 0.0, y = 0.0, z = 0.0;

        for (Direction orientation : directions) {
            switch (orientation) {
                case UP:
                    z += displacement;
                    break;
                case DOWN:
                    z -= displacement;
                    break;
                case RIGHT:
                    y -= displacement;
                    break;
                case LEFT:
                    y += displacement;
                    break;
                case FORWARD:
                    x += displacement;
                    break;
                case BACK:
                    x -= displacement;
                    break;
            }
        }

        return new DirVector((float) x, (float) y, (float) z);
    }
}

