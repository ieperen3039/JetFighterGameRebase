package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Controllers.Controller;

/**
 * Listens to the input stream, executing the controls as soon as they are received
 * The value of every floating point message is [0 - 254], where (0 -> -1, 127 -> 0, 254 -> 1)
 * @author Geert van Ieperen created on 5-5-2018.
 */
public class RemoteControlReceiver implements Controller {

    private float throttle, pitch, yaw, roll;
    private boolean primary, secondary;

    RemoteControlReceiver() {}

    public void receive(MessageType type, int value) {
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
     * @return this value mapped to [-1, 1], where (0 -> -1, 127 -> 0, 254 -> 1)
     */
    public static float toFloat(int value) {
        return (value / 127f) - 1;
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
    public float throttle() {
        return throttle;
    }

    @Override
    public float pitch() {
        return pitch;
    }

    @Override
    public float yaw() {
        return yaw;
    }

    @Override
    public float roll() {
        return roll;
    }

    @Override
    public boolean primaryFire() {
        return primary;
    }

    @Override
    public boolean secondaryFire() {
        return secondary;
    }

    @Override
    public boolean isActiveController() {
        return false;
    }
}
