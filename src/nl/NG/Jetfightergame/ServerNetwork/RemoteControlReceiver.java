package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;

/**
 * Listens to the input stream, executing the controls as soon as they are received
 * The value of every floating point message is [0 - 254], where (0 -> -1, 127 -> 0, 254 -> 1)
 * @author Geert van Ieperen created on 5-5-2018.
 */
public class RemoteControlReceiver implements Controller {

    private final GameTimer timer;
    private float throttle, pitch, yaw, roll;
    private boolean primary, secondary;

    private float controlDisabledUntil = 0;
    private boolean disabled = false;

    RemoteControlReceiver(GameTimer timer) {
        this.timer = timer;
    }

    public void receive(MessageType type, int value) {

        // single assignment, so technically thread-safe
        switch (type) {
            case THROTTLE:
                throttle = toFloat(value);
                break;
            case PITCH:
                pitch = toFloat(value);
                break;
            case YAW:
                yaw = toFloat(value);
                break;
            case ROLL:
                roll = toFloat(value);
                break;
            case PRIMARY_FIRE:
                primary = value > 0;
                break;
            case SECONDARY_FIRE:
                secondary = value > 0;
                break;
        }
    }

    /**
     * @param value an int representation in range [0, 254]
     * @return the given value mapped to [-1, 1], where (0 -> -1, 127 -> 0, 254 -> 1)
     */
    private static float toFloat(int value) {
        return Math.min((value / 127f) - 1, 1);
    }

    /**
     * @param value a value in range [-1, 1]
     * @return this value mapped to [0, 254], where (0 -> -1, 127 -> 0, 254 -> 1)
     */
    public static byte toByte(float value) {
        return (byte) ((value + 1) * 127);
    }

    /**
     * @param isTrue a boolean
     * @return a 0 byte if (!isTrue), a 1 byte if (isTrue)
     */
    public static byte toByte(boolean isTrue) {
        return isTrue ? (byte) 1 : (byte) 0;
    }

    @Override
    public void update() {
        disabled = (controlDisabledUntil > timer.time());
    }

    @Override
    public float throttle() {
        if (disabled) return 0;
        return throttle;
    }

    @Override
    public float pitch() {
        if (disabled) return 0;
        return pitch;
    }

    @Override
    public float yaw() {
        if (disabled) return 0;
        return yaw;
    }

    @Override
    public float roll() {
        if (disabled) return 0;
        return roll;
    }

    @Override
    public boolean primaryFire() {
        if (disabled) return false;
        return primary;
    }

    @Override
    public boolean secondaryFire() {
        if (disabled) return false;
        return secondary;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }

    public void disableControl(float disableUntil) {
        disabled = true;
        this.controlDisabledUntil = disableUntil;
    }
}
