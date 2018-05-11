package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameState.EntityManager;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ServerNetwork.RemoteControlReceiver.toByte;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener {
    private final Controller input;
    private final BufferedOutputStream serverOut;
    private final InputStream serverIn;
    private final EntityManager game;

    public ClientConnection(Consumer<Exception> exceptionHandler, Controller playerInput, Socket connection, EntityManager game) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false, exceptionHandler);
        this.input = playerInput;
        serverOut = new BufferedOutputStream(connection.getOutputStream());
        serverIn = connection.getInputStream();
        this.game = game;
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(serverIn.read());
        Toolbox.print("client message:", type);

        if (type == MessageType.ENTITY_UPDATE) {
            JetFighterProtocol.entityUpdateRead(serverIn, game.getEntities());

        } else if (type == MessageType.ENTITY_SPAWN) {
            MovingEntity newEntity = JetFighterProtocol.newEntityRead(serverIn, game, input, game.getTimer());
            game.addEntity(newEntity);

        }

        return type != MessageType.CONNECTION_CLOSE;
    }

    /**
     * creates a new entity in the server. This entity will be sent back, and can be caught by {@link #handleMessage()}
     * @param type type of entity
     * @param position world-space position of spawning
     * @param forward forward-position of the entity, makes most sense when spawning planes
     * @param velocity world-space movement of the plane
     */
    public void createEntity(EntityClass type, PosVector position, DirVector forward, DirVector velocity){
        try {
            Quaternionf rotation = Toolbox.xTo(forward);
            JetFighterProtocol.spawnRequestSend(serverOut, type, position, rotation, velocity);

        } catch (IOException e) {
            Toolbox.printError(e);
        }
    }

    /**
     * sends a message to the server
     * @param type the message to send
     * @param value the value of this message
     * @throws IOException if there is a problem with the connection to the server
     * @throws IllegalArgumentException if the number of value arguments is invalid
     * @throws IllegalArgumentException if any value is out of range
     */
    public void send(MessageType type, byte value) throws IOException, IllegalArgumentException {
        JetFighterProtocol.controlSend(serverOut, type, value);
    }

    @Override
    protected void update(float deltaTime) throws Exception {
        // this block is not required if an active controller is used
        send(MessageType.THROTTLE, toByte(input.throttle()));
        send(MessageType.PITCH, toByte(input.pitch()));
        send(MessageType.YAW, toByte(input.yaw()));
        send(MessageType.ROLL, toByte(input.roll()));
        send(MessageType.PRIMARY_FIRE, input.primaryFire()? (byte) 1 : 0);
        send(MessageType.SECONDARY_FIRE, input.secondaryFire()? (byte) 1 : 0);

        serverOut.flush();
    }

    @Override
    protected void cleanup() {
        try {
            serverIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a request for a jet to the server and reads the final entity
     * @see ServerConnection#getPlayer(GameEntity.State, GameTimer)
     */
    public AbstractJet getPlayer(GameTimer time) throws IOException {
        // wait for confirmation of connection
        int reply = serverIn.read();
        if (MessageType.get(reply) != MessageType.CONFIRM_CONNECTION)
            throw new IOException("Received " + reply + " as reaction on connection");

        JetFighterProtocol.playerSpawnRequest(serverOut, EntityClass.BASIC_JET);
        serverOut.flush();

        return (AbstractJet) JetFighterProtocol.newEntityRead(
                serverIn, game, input, time
        );
    }
}
