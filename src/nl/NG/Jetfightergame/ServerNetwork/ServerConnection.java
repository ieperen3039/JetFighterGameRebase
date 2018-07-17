package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.PowerupType;
import nl.NG.Jetfightergame.AbstractEntities.Prentity;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * can be viewed as a client's personal connection inside the server
 * @author Geert van Ieperen created on 5-5-2018.
 */
public class ServerConnection implements BlockingListener, Player {
    private final InputStream clientIn;
    private final OutputStream clientOut;
    private final String clientName;
    private final boolean hasAdminCapabilities;

    private final GameServer server;
    private final AbstractJet playerJet;
    private final RemoteControlReceiver controls;
    private final JetFighterProtocol protocol;

    private Lock sendLock = new ReentrantLock();
    private volatile boolean isClosed;
    private PowerupType currentPowerup;

    /**
     * construct a server-side connection to a player
     * @param inputStream  the incoming communication from the player
     * @param outputStream the outgoing communication to the player
     * @param server       the object that accepts server-commands
     * @param spawnAccept  a function that takes the spawn and id of an AbstarctJet, and communicates this to the other
     *                     players. The entity is guaranteed to be of type AbstractJet
     * @param playerSpawn  the place and state of the player at the moment of spawning. this should be an unoccupied
     *                     place in space
     * @param worldType    the current selected world
     * @param isAdmin      if true, allows the player to send commands of host level.
     * @throws IOException if any communication error occurs, as defined by the given Input- and OutputStreams
     */
    public ServerConnection(
            InputStream inputStream, OutputStream outputStream,
            GameServer server, BiConsumer<Prentity, Integer> spawnAccept, MovingEntity.State playerSpawn,
            EnvironmentClass worldType, boolean isAdmin
    ) throws IOException {
        this.clientOut = new BufferedOutputStream(outputStream);
        this.clientIn = inputStream;
        this.hasAdminCapabilities = isAdmin;
        this.server = server;
        this.controls = new RemoteControlReceiver();
        this.protocol = new JetFighterProtocol(clientIn, clientOut);

        // get player properties
        protocol.syncTimerSource(server.getTimer());
        protocol.worldSwitchSend(worldType);
        clientOut.flush();
        Pair<String, AbstractJet> p = protocol.playerSpawnAccept(playerSpawn, server, controls, spawnAccept);
        clientName = p.left;
        playerJet = p.right;
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(clientIn.read());

        if (type == MessageType.CONNECTION_CLOSE) {
            isClosed = true;
            clientIn.close();
            clientOut.close();
            Logger.print("Connection to " + clientName + " has been closed");
            return false;

        } else if (type.isOf(MessageType.adminOnly) && !hasAdminCapabilities) {
            Logger.printError(this + " sent a " + type + " command, while it has no access to it");
            return true;
        }

        if (type.isOf(MessageType.controls)) {
            protocol.controlRead(controls, type);

        } else switch (type) {
            case PING:
                sendMessage(MessageType.PONG, clientOut::flush);
                break;

            case UNPAUSE_GAME:
                server.unPause();
                break;

            case PAUSE_GAME:
                server.pause();
                break;

            case START_GAME:
                server.startRace();
                break;

            case SHUTDOWN_GAME:
                server.shutDown();
                break;

            default:
                long bits = clientIn.skip(type.nOfArgs());
                Logger.printError("Message caused an error: " + type, "skipping " + bits + " bits");
        }

        return true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * sends an update to the client of the given entity's position, rotation and velocity
     * @param entity      the entity to be updated
     * @param currentTime the time of when this entity is on the said position
     */
    public void sendEntityUpdate(MovingEntity entity, float currentTime) {
        sendMessage(MessageType.ENTITY_UPDATE, () ->
                protocol.entityUpdateSend(entity, currentTime)
        );
    }

    /**
     * sends the event of a newly spawned entity
     * @param entity the entity to be sent
     * @param id     its unique id, generated by the server
     */
    public void sendEntitySpawn(Prentity entity, int id) {
        sendMessage(MessageType.ENTITY_SPAWN, () ->
                protocol.newEntitySend(entity, id)
        );
    }

    public void sendExplosionSpawn(PosVector position, DirVector direction, float spread, Color4f color1, Color4f color2) {
        sendMessage(MessageType.EXPLOSION_SPAWN, () ->
                protocol.explosionSend(position, direction, spread, color1, color2)
        );
    }

    public void sendEntityRemove(MovingEntity entity) {
        sendMessage(MessageType.ENTITY_REMOVE, () ->
                protocol.entityRemoveSend(entity)
        );
    }

    public void sendProgress(String playerName, int checkPointNr, int roundNr) {
        sendMessage(MessageType.RACE_PROGRESS, () ->
                protocol.raceProgressSend(playerName, checkPointNr, roundNr)
        );
    }

    public void sendPlayerSpawn(Player player) {
        sendMessage(MessageType.PLAYER_SPAWN, () ->
                protocol.playerSpawnSend(player.playerName(), player.jet())
        );
    }

    public void sendWorldSwitch(EnvironmentClass world) {
        sendMessage(MessageType.WORLD_SWITCH, () ->
                protocol.worldSwitchSend(world)
        );
    }

    private void sendMessage(MessageType type, IOAction action) {
        sendLock.lock();
        try {
            clientOut.write(type.ordinal());
            action.run();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            sendLock.unlock();
        }
    }

    /** send the previously collected data to the clients */
    public void flush() {
        sendLock.lock();
        try {
            clientOut.flush();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            sendLock.unlock();
        }
    }

    public void sendShutDown() {
        if (!isClosed) sendMessage(MessageType.SHUTDOWN_GAME, clientOut::flush);
    }

    @Override
    public String toString() {
        return "player " + playerName();
    }

    @Override
    public AbstractJet jet() {
        return playerJet;
    }

    @Override
    public PowerupType getCurrentPowerup() {
        return currentPowerup;
    }

    @Override
    public boolean addPowerup(PowerupType.Primitive type) {
        PowerupType next = currentWith(type);
        if (next == currentPowerup) return false;

        sendMessage(MessageType.POWERUP_COLLECT, () ->
                protocol.powerupCollectSend(type)
        );
        currentPowerup = next;
        return true;
    }

    @Override
    public String playerName() {
        return clientName;
    }

    /** executes the action, which may throw an IOException */
    private interface IOAction {
        void run() throws IOException;
    }
}
