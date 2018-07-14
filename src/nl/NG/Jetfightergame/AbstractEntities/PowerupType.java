package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.util.EnumSet;

import static nl.NG.Jetfightergame.AbstractEntities.PowerupType.Primitive.*;

/**
 * @author Geert van Ieperen. Created on 11-7-2018.
 */
public enum PowerupType {
    SPEED(TIME), SHIELD(INFO), ROCKET(ENERGY), SMOKE(SPACE);

    private static final EnumSet<PowerupType> VALUE_SET = EnumSet.allOf(PowerupType.class);
    private static final PowerupType[] VALUE_ARRAY = values();

    private final EnumSet<Primitive> required;

    PowerupType(Primitive r, Primitive... o) {
        required = EnumSet.of(r, o);
    }

    /**
     * finds a powerup with the given type
     * @param resources the resources
     * @return the powerup that encapsulates exactly the given primitives
     */
    public static PowerupType get(EnumSet<Primitive> resources) {
        return VALUE_SET.stream()
                .filter(pwr -> pwr.required.containsAll(resources))
                .filter(pwr -> resources.containsAll(pwr.required))
                .findAny()
                .orElse(null);
    }

    public static PowerupType get(Primitive source) {
        switch (source) {
            case TIME:
                return SPEED;
            case SPACE:
                return SMOKE;
            case ENERGY:
                return ROCKET;
            case INFO:
                return SHIELD;
            default:
                throw new UnsupportedOperationException("unknown enum constant" + source);
        }
    }

    public static PowerupType get(int id) {
        if (id >= VALUE_ARRAY.length) throw new IllegalArgumentException("Invalid power-up identifier " + id);
        else return VALUE_ARRAY[id];
    }

    public PowerupType with(Primitive type) {
        EnumSet<Primitive> types = required.clone();
        types.add(type);
        return get(types);
    }

    /** building blocks of powerups */
    public enum Primitive {
        TIME(Color4f.GREEN),
        SPACE(Color4f.YELLOW),
        ENERGY(Color4f.RED),
        INFO(Color4f.BLUE);

        private static final Primitive[] PRIMITIVES = values();
        public final Color4f color;

        Primitive(Color4f color) {
            this.color = color;
        }

        public static Primitive get(int id) {
            if (id >= PRIMITIVES.length)
                throw new IllegalArgumentException("Invalid power-up primitive identifier " + id);
            else return PRIMITIVES[id];
        }
    }
}
