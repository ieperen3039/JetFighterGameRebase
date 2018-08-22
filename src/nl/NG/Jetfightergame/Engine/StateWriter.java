package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.ServerNetwork.EnvironmentClass;
import nl.NG.Jetfightergame.ServerNetwork.MessageType;
import nl.NG.Jetfightergame.ServerNetwork.ServerConnection;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;

/**
 * @author Geert van Ieperen. Created on 20-8-2018.
 */
public class StateWriter extends ServerConnection {
    public static final String EXTENSION = ".jfgr";
    private static final Set<MessageType> filteredMessages =
            EnumSet.of(PAUSE_GAME, UNPAUSE_GAME, PING, PONG, SYNC_TIMER);
    private int worldSwitches = 0;

    public StateWriter(float currentTime) throws IOException {
        super("StateWriter", getfile(), currentTime);
    }

    @Override
    public void send(MessageType messageType) {
        if (filteredMessages.contains(messageType)) return;
        Logger.INFO.print(messageType);
        super.send(messageType);
    }

    private static File getfile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm"); // day-minute
        String name = "Replay_" + dateFormat.format(new Date()) + EXTENSION;
        return Directory.recordings.getFile(name);
    }

    @Override
    public String toString() {
        return "StateWriter";
    }

    @Override
    public boolean handleMessage() throws IOException {
        return false;
    }

    @Override
    public boolean isClosed() {
        return worldSwitches != 1;
    }

    @Override
    public void sendWorldSwitch(EnvironmentClass world, float countDown, int maxRounds) {
        worldSwitches++;
        if (worldSwitches == 2) closeOutputStream();
        super.sendWorldSwitch(world, countDown, maxRounds);
    }

    @Override
    public void sendProgress(int pInd, int checkPointNr, int roundNr) {
        super.sendProgress(pInd, checkPointNr, roundNr);
    }

    @Override
    protected void closeOutputStream() {
        try {
            super.closeOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
