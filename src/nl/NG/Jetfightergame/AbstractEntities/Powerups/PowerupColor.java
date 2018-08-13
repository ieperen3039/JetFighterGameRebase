package nl.NG.Jetfightergame.AbstractEntities.Powerups;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/** building blocks of powerups */
public enum PowerupColor {
    NONE(Color4f.INVISIBLE),
    GREEN(Color4f.GREEN),
    YELLOW(Color4f.YELLOW),
    RED(Color4f.RED),
    BLUE(Color4f.BLUE);

    private static final PowerupColor[] PRIMITIVES = values();
    public final Color4f color;

    PowerupColor(Color4f color) {
        this.color = color;
    }

    public static PowerupColor get(int id) {
        if (id >= PRIMITIVES.length)
            throw new IllegalArgumentException("Invalid power-up primitive identifier " + id);
        else return PRIMITIVES[id];
    }
}
