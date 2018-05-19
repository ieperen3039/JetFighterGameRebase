package nl.NG.Jetfightergame.ServerNetwork;

import java.util.EnumSet;

/**
 * Important note, there may not be more than 255 constants defined here due to implementation assumptions.
 * Visa versa, you may assume that (MessageType).ordinal() always fits in a byte.
 * @author Geert van Ieperen, created on 5-5-2018.
 */
public enum MessageType {
    WRONG_MESSAGE_0, WRONG_MESSAGE_1, // for ease of debugging, these messages may not be used
    CONNECTION_CLOSE, CONFIRM_CONNECTION, // CONNECTION_CLOSE counts also for id = -1
    PING, PONG,
    START_GAME, PAUSE_GAME, SHUTDOWN_GAME,
    THROTTLE, PITCH, YAW, ROLL, PRIMARY_FIRE, SECONDARY_FIRE,
    ENTITY_UPDATE, ENTITY_SPAWN, PLAYER_SPAWN, EXPLOSION_SPAWN;

    public static EnumSet<MessageType> controls = EnumSet.of(THROTTLE, PITCH, YAW, ROLL, PRIMARY_FIRE, SECONDARY_FIRE);
    public static EnumSet<MessageType> adminOnly = EnumSet.of(START_GAME, SHUTDOWN_GAME);
    public static EnumSet<MessageType> lobbyCommands = EnumSet.of(START_GAME);
    public static EnumSet<MessageType> variableLength = EnumSet.of(ENTITY_SPAWN, ENTITY_UPDATE, PLAYER_SPAWN);

    /**
     * @param id a number n corresponing to an enum ordinal
     * @return the enum e such that {@code e.ordinal() == n}
     * @throws IllegalArgumentException if the id does not correspond to a valid message
     */
    public static MessageType get(int id) {
        if (id >= values().length) throw new IllegalArgumentException("Invalid message identifier " + id);
        else if (id == -1) return CONNECTION_CLOSE;
        else return values()[id];
    }

    /**
     * @param set a set of MessageTypes
     * @return true if this message is part of the set
     */
    public boolean isOf(EnumSet<MessageType> set){
        return set.contains(this);
    }

    /** @return the number of values must be send after this message, or Integer.MAX_VALUE if this is undetermined */
    public int nOfArgs() {
        if (isOf(controls)) return 1;
        if (isOf(variableLength)) return Integer.MAX_VALUE;
        else return 0;
    }
}
