package nl.NG.Jetfightergame.ServerNetwork;

import java.util.EnumSet;

/**
 * NOTE: There may not be more than 255 constants defined here due to implementation assumptions.
 * Visa versa, you may assume that (MessageType).ordinal() always fits in a byte.
 * @author Geert van Ieperen, created on 5-5-2018.
 */
public enum MessageType {
    INVALID_MESSAGE_ID_0, // for ease of debugging, this messages may not be used
    CONFIRM_CONNECTION, CLOSE_REQUEST, CONNECTION_CLOSE, // CONNECTION_CLOSE also matches id = -1
    TEXT_MESSAGE, SYNC_TIMER,
    PING, PONG,
    PAUSE_GAME, UNPAUSE_GAME, START_GAME, SHUTDOWN_GAME, WORLD_SWITCH,
    THROTTLE, PITCH, YAW, ROLL, PRIMARY_FIRE, SECONDARY_FIRE,
    ENTITY_UPDATE, ENTITY_SPAWN, ENTITY_REMOVE, EXPLOSION_SPAWN, BOOSTER_COLOR_CHANGE,
    PLAYER_SPAWN, PLAYER_UPDATE, RACE_PROGRESS, POWERUP_STATE, POWERUP_COLLECT;

    private static final MessageType[] VALUES = values();
    public static EnumSet<MessageType> controls = EnumSet.of(THROTTLE, PITCH, YAW, ROLL, PRIMARY_FIRE, SECONDARY_FIRE);
    public static EnumSet<MessageType> adminOnly = EnumSet.of(START_GAME, PAUSE_GAME, UNPAUSE_GAME, SHUTDOWN_GAME);
    public static EnumSet<MessageType> variableLength = EnumSet.of(ENTITY_SPAWN, ENTITY_UPDATE, PLAYER_SPAWN, PLAYER_UPDATE);

    /**
     * @param id a number n corresponing to an enum ordinal
     * @return the enum e such that {@code e.ordinal() == n}
     * @throws IllegalArgumentException if the id does not correspond to a valid message
     */
    public static MessageType get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid message identifier " + id);
        else if (id == -1) return CONNECTION_CLOSE;
        else return VALUES[id];
    }

    public static String asString(int id) {
        if (id >= VALUES.length || id < -1) {
            return id + " (Invalid message id)";
        } else if (id == -1) {
            return id + " (Connection close)";
        } else {
            return VALUES[id].toString();
        }
    }

    /**
     * @param set a set of MessageTypes
     * @return true if this message is part of the set
     */
    public boolean isOf(EnumSet<MessageType> set){
        return set.contains(this);
    }

    /** @return the number of values must be send after this message, or Integer.MAX_VALUE if this is undetermined */
    public int nOfBits() {
        if (isOf(controls) || (this == ENTITY_REMOVE)) return 1;
        if (isOf(variableLength)) return Integer.MAX_VALUE;
        else return 0;
    }
}
