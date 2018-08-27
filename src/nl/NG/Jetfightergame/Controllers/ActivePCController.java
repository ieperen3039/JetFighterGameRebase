package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.ServerNetwork.RemoteControlReceiver;
import nl.NG.Jetfightergame.Tools.Logger;

import java.io.IOException;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

/**
 * @author Geert van Ieperen. Created on 30-6-2018.
 */
public class ActivePCController extends PassivePCControllerAbsolute implements TrackerKeyListener {
    private final ClientConnection target;

    private float throttle = 0;
    private float pitch = 0;
    private float yaw = 0;
    private float roll = 0;
    private boolean primary = false;
    private boolean secondary = false;

    public ActivePCController(ClientConnection target) {
        this.target = target;
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        super.mouseMoved(deltaX, deltaY);
        update();
    }

    @Override
    public void keyPressed(int key) {
        update();
    }

    @Override
    public void update() {
        try {
            throttle = updateAxis(throttle, throttle(), MessageType.THROTTLE);
            pitch = updateAxis(pitch, pitch(), MessageType.PITCH);
            yaw = updateAxis(yaw, yaw(), MessageType.YAW);
            roll = updateAxis(roll, roll(), MessageType.ROLL);
            primary = updateButton(primary, primaryFire(), MessageType.PRIMARY_FIRE);
            secondary = updateButton(secondary, secondaryFire(), MessageType.SECONDARY_FIRE);

        } catch (IOException e) {
            Logger.ERROR.print(e);
        }
    }

    private boolean updateButton(boolean field, boolean newValue, MessageType type) throws IOException {
        if (field != newValue) {
            target.sendControl(type, RemoteControlReceiver.toByte(field));
            field = newValue;
        }
        return field;
    }

    private float updateAxis(float field, float newValue, MessageType type) throws IOException {
        if (field != newValue) {
            target.sendControl(type, RemoteControlReceiver.toByte(newValue));
            field = newValue;
        }
        return field;
    }

    @Override
    public Consumer<ScreenOverlay.Painter> hudElement() {
        return (o) -> glfwPollEvents();
    }

    @Override
    public boolean isActiveController() {
        return true;
    }
}
