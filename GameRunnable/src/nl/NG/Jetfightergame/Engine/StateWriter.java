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
    private final File file;
    private int worldSwitches = 0;

    /** @see #StateWriter(float, File) */
    public StateWriter(float currentTime) throws IOException {
        this(currentTime, getfile());
    }

    /**
     * a connection to a file, that writes its state to a file. This player does not have a valid jet.
     * @param currentTime the time of the server when starting
     * @param file        the file to write to
     * @throws IOException if a file exception occurs
     */
    public StateWriter(float currentTime, File file) throws IOException {
        super("StateWriter", file, currentTime);
        this.file = file;
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
        return "StateWriter (" + file.getName() + ")";
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
    protected void closeOutputStream() {
        try {
            super.closeOutputStream();
            Logger.INFO.print("Stored recording to file " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
