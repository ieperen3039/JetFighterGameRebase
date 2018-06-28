package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;
import static nl.NG.Jetfightergame.ServerNetwork.RemoteControlReceiver.toByte;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener, SpawnReceiver {
    private final Player player;

    private final BufferedOutputStream serverOut;
    private final InputStream serverIn;
    private final Environment game;

    private Lock sendLock = new ReentrantLock();

    public ClientConnection(Socket connection, Environment game) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false);
        this.serverOut = new BufferedOutputStream(connection.getOutputStream());
        this.serverIn = connection.getInputStream();
        this.game = game;

        JetFighterProtocol.syncTimerTarget(serverIn, serverOut, game.getTimer());

        String name = connection.getInetAddress().getHostName();
        player = new Player(name);
        AbstractJet playerJet = getPlayerJet(player.getInput());
        player.setJet(playerJet);
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(serverIn.read());

        if (type == MessageType.ENTITY_SPAWN) {
            MovingEntity newEntity = JetFighterProtocol.newEntityRead(serverIn, this, Controller.EMPTY);
            game.addEntity(newEntity);

        } else if (type == MessageType.ENTITY_UPDATE) {
            JetFighterProtocol.entityUpdateRead(serverIn, game.getEntities());

        } else if (type == MessageType.ENTITY_REMOVE) {
            int target = JetFighterProtocol.entityRemoveRead(serverIn);
            MovingEntity entity = game.getEntity(target);
            game.removeEntity(entity);

            if (entity instanceof TemporalEntity) {
                ParticleCloud explosion = ((TemporalEntity) entity).explode();
                game.addParticles(explosion);
            }

        } else if (type == MessageType.SHUTDOWN_GAME) {
            /* triggers {@link #cleanup()}*/
            stopLoop();

        } else if (type == MessageType.CONNECTION_CLOSE) {
            return false;

        } else if (type == MessageType.EXPLOSION_SPAWN) {
            ParticleCloud cloud = JetFighterProtocol.explosionRead(serverIn);
            game.addParticles(cloud);

        } else {
            Logger.print("unknown message: " + type);
        }

        return true;
    }

    /**
     * sends a single command to the server
     */
    public void sendCommand(MessageType type) {
        sendLock.lock();
        try {
            serverOut.write(type.ordinal());
            serverOut.flush();

        } catch (IOException ex) {
            Logger.printError(ex);

        } finally {
            sendLock.unlock();
        }
    }

    @Override
    public void addSpawn(MovingEntity.Spawn spawn) {
        game.addEntity(spawn.construct(this, null));
    }

    /**
     * sends a single control message to the server
     * @param type the control to adapt
     * @param value the value of this message
     * @throws IOException if there is a problem with the connection to the server
     * @throws IllegalArgumentException if the number of value arguments is invalid
     * @throws IllegalArgumentException if any value is out of range
     */
    public void sendControl(MessageType type, byte value) throws IOException, IllegalArgumentException {
        sendLock.lock();
        try {
            serverOut.write(type.ordinal());
            JetFighterProtocol.controlSend(serverOut, value);
            serverOut.flush();

        } finally {
            sendLock.unlock();
        }
    }

    @Override
    // this block is not required if an active controller is used
    protected void update(float deltaTime) throws Exception {
        Controller input = player.getInput();
        sendLock.lock();
        try {
            // axis controls
            serverOut.write(THROTTLE.ordinal());
            JetFighterProtocol.controlSend(serverOut, toByte(input.throttle()));
            serverOut.write(PITCH.ordinal());
            JetFighterProtocol.controlSend(serverOut, toByte(input.pitch()));
            serverOut.write(YAW.ordinal());
            JetFighterProtocol.controlSend(serverOut, toByte(input.yaw()));
            serverOut.write(ROLL.ordinal());
            JetFighterProtocol.controlSend(serverOut, toByte(input.roll()));
            // binary controls
            byte doPrimary = input.primaryFire() ? (byte) 1 : (byte) 0;
            serverOut.write(PRIMARY_FIRE.ordinal());
            JetFighterProtocol.controlSend(serverOut, doPrimary);
            byte doSecondary = input.secondaryFire() ? (byte) 1 : (byte) 0;
            serverOut.write(SECONDARY_FIRE.ordinal());
            JetFighterProtocol.controlSend(serverOut, doSecondary);

        } finally {
            sendLock.unlock();
        }

        serverOut.flush();
    }

    @Override
    public void stopLoop() {
        sendLock.lock();
        try {
            serverOut.write(CONNECTION_CLOSE.ordinal());
            serverOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendLock.unlock();
        }

        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        try {
            serverIn.close();
            Logger.print(this + " connection close");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a request for a jet to the server and reads the final entity
     * @see ServerConnection#createPlayer(GameEntity.State)
     * @param input
     */
    private AbstractJet getPlayerJet(Controller input) throws IOException {
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
    public GameTimer getTimer() {
        return game.getTimer();
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, Color4f color1, Color4f color2, float power, int density) {
        game.addParticles(Particles.explosion(position, direction, color1, color2, power, density));
    }

    public Player getPlayer() {
        return player;
    }
}
