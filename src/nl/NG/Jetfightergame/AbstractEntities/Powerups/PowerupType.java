package nl.NG.Jetfightergame.AbstractEntities.Powerups;

import java.util.EnumSet;

import static nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupColor.*;

/**
 * @author Geert van Ieperen. Created on 11-7-2018.
 */
public enum PowerupType {
    NONE(PowerupColor.NONE), SPEED(TIME), SHIELD(INFO), ROCKET(ENERGY), SMOKE(SPACE);

    public static final float SEEKER_LAUNCH_SPEED = 3f;
    public static final float SMOKE_LAUNCH_SPEED = 20f;
    public static final float SMOKE_SPREAD = 10f;
    public static final int SMOKE_DENSITY = 1_000;
    public static final float SPEED_BOOST_DURATION = 5f;
    public static final float SPEED_BOOST_FACTOR = 2f;
    public static final float SMOKE_LINGER_TIME = 30f;
    private static final EnumSet<PowerupType> VALUE_SET = EnumSet.allOf(PowerupType.class);
    private static final PowerupType[] VALUE_ARRAY = values();

    private final EnumSet<PowerupColor> required;

    PowerupType(PowerupColor r, PowerupColor... o) {
        required = EnumSet.of(r, o);
    }

    /**
     * finds a powerup with the given type
     * @param resources the resources
     * @return the powerup that encapsulates exactly the given primitives
     */
    public static PowerupType get(EnumSet<PowerupColor> resources) {
        return VALUE_SET.stream()
                .filter(pwr -> pwr.required.containsAll(resources))
                .filter(pwr -> resources.containsAll(pwr.required))
                .findAny()
                .orElse(NONE);
    }

    public static PowerupType get(PowerupColor source) {
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

    public PowerupType with(PowerupColor type) {
        if (this == NONE) return get(type);

        EnumSet<PowerupColor> types = required.clone();
        types.add(type);
        return get(types);
    }
}
