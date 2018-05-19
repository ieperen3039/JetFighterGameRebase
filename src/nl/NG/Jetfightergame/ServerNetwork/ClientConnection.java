package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;
import static nl.NG.Jetfightergame.ServerNetwork.RemoteControlReceiver.toByte;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener, SpawnReceiver {
    private final Controller input;
    private final BufferedOutputStream serverOut;
    private final InputStream serverIn;
    private final Environment game;
    private final AbstractJet playerJet;

    private Lock sendLock = new ReentrantLock();

    public ClientConnection(Consumer<Exception> exceptionHandler, Controller playerInput, Socket connection, Environment game) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false, exceptionHandler);
        this.input = playerInput;
        this.serverOut = new BufferedOutputStream(connection.getOutputStream());
        this.serverIn = connection.getInputStream();
        this.game = game;

        JetFighterProtocol.syncTimerTarget(serverIn, serverOut, game.getTimer());

        this.playerJet = createPlayer();
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(serverIn.read());

        if (type == MessageType.ENTITY_SPAWN) {
            MovingEntity newEntity = JetFighterProtocol.newEntityRead(serverIn, this, Controller.EMPTY);
            game.addEntity(newEntity);

        } else if (type == MessageType.ENTITY_UPDATE) {
            JetFighterProtocol.entityUpdateRead(serverIn, game.getEntities());

        } else if (type == MessageType.SHUTDOWN_GAME) {
            stopLoop();

        } else if (type != MessageType.CONNECTION_CLOSE) {
            serverIn.close();
            return false;
        }

        return true;
    }

    public void sendCommand(MessageType type) {
        sendLock.lock();
        try {
            serverOut.write(type.ordinal());
            serverOut.flush();

        } catch (IOException ex) {
            Toolbox.printError(ex);

        } finally {
            sendLock.unlock();
        }
    }

    @Override
    public void addSpawn(MovingEntity.Spawn spawn) {
        sendLock.lock();
        try {
            serverOut.write(MessageType.ENTITY_SPAWN.ordinal());
            JetFighterProtocol.spawnRequestSend(serverOut, spawn);

        } catch (IOException e) {
            Toolbox.printError(e);

        } finally {
            sendLock.unlock();
        }
    }

    /**
     * creates a new entity in the server. This entity will be sent back, and can be caught by {@link #handleMessage()}
     * @param type type of entity
     * @param position world-space position of spawning
     * @param forward forward-position of the entity, makes most sense when spawning planes
     * @param velocity world-space movement of the plane
     */
    public void createEntity(EntityClass type, PosVector position, DirVector forward, DirVector velocity){
        addSpawn(new MovingEntity.Spawn(type, position, Toolbox.xTo(forward), velocity));
    }

    /**
     * sends a message to the server
     * @param type the message to send
     * @param value the value of this message
     * @throws IOException if there is a problem with the connection to the server
     * @throws IllegalArgumentException if the number of value arguments is invalid
     * @throws IllegalArgumentException if any value is out of range
     */
    public synchronized void sendControl(MessageType type, byte value) throws IOException, IllegalArgumentException {
        sendLock.lock();
        try {
            serverOut.write(type.ordinal());
            JetFighterProtocol.controlSend(serverOut, value);

        } finally {
            sendLock.unlock();
        }
    }

    @Override
    protected void update(float deltaTime) throws Exception {
        // this block is not required if an active controller is used
        sendControl(THROTTLE, toByte(input.throttle()));
        sendControl(PITCH, toByte(input.pitch()));
        sendControl(YAW, toByte(input.yaw()));
        sendControl(ROLL, toByte(input.roll()));
        sendControl(PRIMARY_FIRE, input.primaryFire() ? (byte) 1 : (byte) 0);
        sendControl(SECONDARY_FIRE, input.secondaryFire() ? (byte) 1 : (byte) 0);

        serverOut.flush();
    }

    @Override
    protected void cleanup() {
        try {
            sendLock.lock(); // lock sending forever
            serverOut.write(CONNECTION_CLOSE.ordinal());
            serverOut.close(); // also flushes

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a request for a jet to the server and reads the final entity
     * @see ServerConnection#createPlayer(GameEntity.State)
     */
    private AbstractJet createPlayer() throws IOException {
        // wait for confirmation of connection
        int reply = serverIn.read();
        if (reply != MessageType.CONFIRM_CONNECTION.ordinal())
            throw new IOException("Received " + MessageType.get(reply) + " as reaction on connection");

        JetFighterProtocol.playerSpawnRequest(serverOut, EntityClass.BASIC_JET);
        serverOut.flush();

        return (AbstractJet) JetFighterProtocol.newEntityRead(
                serverIn, this, input
        );
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, float spread, Color4f color1, Color4f color2) {
        game.addParticles(newParticles);
    }

    @Override
    public GameTimer getTimer() {
        return game.getTimer();
    }

    public AbstractJet getPlayer() {
        return playerJet;
    }
}
