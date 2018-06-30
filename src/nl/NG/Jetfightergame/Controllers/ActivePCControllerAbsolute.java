package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Controllers.InputHandling.TrackerKeyListener;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;

import java.io.IOException;

import static nl.NG.Jetfightergame.ServerNetwork.RemoteControlReceiver.toByte;

/**
 * @author Geert van Ieperen. Created on 30-6-2018.
 */
public class ActivePCControllerAbsolute extends PassivePCControllerAbsolute implements TrackerKeyListener {
    private final ClientConnection target;

    public ActivePCControllerAbsolute(ClientConnection target) {
        this.target = target;
    }

    @Override
    public void mouseMoved(int deltaX, int deltaY) {
        super.mouseMoved(deltaX, deltaY);
        try {
            target.sendControl(MessageType.PITCH, toByte(pitch()));
            target.sendControl(MessageType.ROLL, toByte(roll()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(int key) {
        try {
            target.sendControl(MessageType.YAW, toByte(yaw()));
            target.sendControl(MessageType.THROTTLE, toByte(throttle()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clickEvent(int x, int y) {
        super.clickEvent(x, y);
        try {
            target.sendControl(MessageType.PRIMARY_FIRE, toByte(primaryFire()));
            target.sendControl(MessageType.SECONDARY_FIRE, toByte(secondaryFire()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActiveController() {
        return true;
    }
}
