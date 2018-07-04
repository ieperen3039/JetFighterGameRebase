package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Particles.DataIO;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 9-5-2018.
 */
public final class JetFighterProtocol {

    /**
     * writes the given entity to the DataOutputStream.
     * @see #entityUpdateRead(DataInputStream, GameState)
     */
    public static void entityUpdateSend(DataOutputStream output, MovingEntity thing, float currentTime) throws IOException {
        // identity and time
        output.writeInt(thing.idNumber());
        output.writeFloat(currentTime);
        // state
        DataIO.writeVector(output, thing.getPosition());
        DataIO.writeQuaternion(output, thing.getRotation());
    }

    /**
     * read an entity from the DataInputStream, match the entity with one in the list, and updates it
     * @param input    the input stream, with an entity state on its next read chunk.
     * @param entities a list of all entities of which one of them must be the one on the stream
     * @return the relevant entity, or null if it was not found
     * @throws IOException if anything goes wrong with the connection
     * @see #entityUpdateRead(DataInputStream, GameState)
     */
    public static MovingEntity entityUpdateRead(DataInputStream input, GameState entities) throws IOException {
        // identity and time
        int id = input.readInt();
        float time = input.readFloat();
        // state
        PosVector pos = DataIO.readPosVector(input);
        Quaternionf rot = DataIO.readQuaternion(input);
        // set the entity state
        MovingEntity target = entities.getEntity(id);
        assert target != null : "Entity with id " + id + " not found";

        target.addStatePoint(time, pos, rot);
        return target;
    }

    /**
     * client sending a request of spawing a new entity.
     * @see #spawnRequestRead(DataInputStream)
     */
    public static void spawnRequestSend(DataOutputStream output, Spawn spawn) throws IOException {
        output.write(spawn.type.ordinal());
        // state
        DataIO.writeVector(output, spawn.position);
        DataIO.writeQuaternion(output, spawn.rotation);
        DataIO.writeVector(output, spawn.velocity);
    }

    /**
     * server reading an entity from the DataInputStream
     * @return a description of a new entity to be added to the server
     * @see #spawnRequestSend(DataOutputStream, Spawn)
     */
    public static Spawn spawnRequestRead(DataInputStream input) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        // state
        PosVector position = DataIO.readPosVector(input);
        Quaternionf rotation = DataIO.readQuaternion(input);
        DirVector velocity = DataIO.readDirVector(input);

        return new Spawn(type, position, rotation, velocity);
    }

    /** server sending a new entity */
    public static void newEntitySend(DataOutputStream output, Spawn entity, int id) throws IOException {
        output.write(entity.type.ordinal());
        // identity number
        output.writeInt(id);
        // state
        DataIO.writeVector(output, entity.position);
        DataIO.writeQuaternion(output, entity.rotation);
        DataIO.writeVector(output, entity.velocity);
    }

    /** client reading an entity off the DataInputStream and creates an instance of it */
    public static MovingEntity newEntityRead(DataInputStream input, SpawnReceiver world, Controller controller) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        // identity number
        int id = input.readInt();
        // state
        PosVector position = DataIO.readPosVector(input);
        Quaternionf rotation = DataIO.readQuaternion(input);
        DirVector velocity = DataIO.readDirVector(input);

        return type.construct(id, world, controller, position, rotation, velocity);
    }

    /** read a control message off the DataInputStream */
    static void controlRead(DataInputStream clientIn, RemoteControlReceiver controls, MessageType type) throws IOException {
        int value = clientIn.read();
        controls.receive(type, value);
    }

    /** sends a control message into the DataOutputStream */
    static synchronized void controlSend(DataOutputStream output, byte value) throws IOException {
        output.write(value);
    }

    /**
     * sends a request to spawn a new plane. When successful, the reply is caught with {@link
     * #newEntityRead(DataInputStream, SpawnReceiver, Controller)}
     */
    public static Pair<String, Spawn> playerSpawnAccept(DataInputStream input, MovingEntity.State position) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        String name = input.readUTF();
        Spawn spawn = new Spawn(type, position);
        return new Pair<>(name, spawn);
    }

    /** @see #playerSpawnAccept(DataInputStream, MovingEntity.State) */
    public static void playerSpawnRequest(DataOutputStream output, EntityClass type, String name) throws IOException {
        output.write(type.ordinal());
        output.writeUTF(name);
    }

    /**
     * sends spawning of a player different from this player
     * @param world entities generated by this player are provided here
     */
    public static Player playerSpawnRead(DataInputStream input, EntityMapping world) throws IOException {
        int id = input.readInt();
        String name = input.readUTF();

        MovingEntity entity = world.getEntity(id);
        assert entity != null : "Entity with id " + id + " not found";
        assert entity instanceof AbstractJet : "received an non-jet entity from the server for player " + name;
        return new ClientConnection.OtherPlayer((AbstractJet) entity, name);
    }

    /**
     * Sends the spawn of player b, different than this player. The entity used by player b must already be sent.
     * @param name   the name-identifier of player b
     * @param entity the entity used by player b
     */
    public static void playerSpawnSend(DataOutputStream output, String name, MovingEntity entity) throws IOException {
        output.write(entity.idNumber());
        output.writeUTF(name);
    }

    /**
     * sets up synchronizing time across server-client connection
     * @param serverTime current time according to the source
     */
    public static void syncTimerSource(DataInputStream input, DataOutputStream output, GameTimer serverTime) throws IOException {
        float deltaNanos = ping(input, output);

        output.writeFloat(serverTime.time() + deltaNanos);
        output.flush();
    }

    /**
     * updates the timer to sourceTime + ping
     */
    public static GameTimer syncTimerTarget(DataInputStream input, DataOutputStream output) throws IOException {
        // wait for signal to arrive, to let the source measure the delay
        // repeat and take average for more accurate results
        pong(input, output);

        float serverTime = input.readFloat();
        return new GameTimer(serverTime);
    }

    /** sends an explosion or other effect to the client
     * @see #explosionRead(DataInputStream)  */
    public static void explosionSend(DataOutputStream output, PosVector position, DirVector direction, float spread, Color4f color1, Color4f color2) throws IOException {
        DataIO.writeVector(output, position);
        DataIO.writeVector(output, direction);
        output.writeFloat(spread);
        DataIO.writeColor(output, color1);
        DataIO.writeColor(output, color2);
    }

    /** reads an explosion off the DataInputStream
     * @see #explosionSend(DataOutputStream, PosVector, DirVector, float, Color4f, Color4f)  */
    public static ParticleCloud explosionRead(DataInputStream input) throws IOException {
        PosVector position = DataIO.readPosVector(input);
        DirVector direction = DataIO.readDirVector(input);
        float power = input.readFloat();
        Color4f color1 = DataIO.readColor(input);
        Color4f color2 = DataIO.readColor(input);

        return Particles.explosion(position, direction, color1, color2, power, ClientSettings.EXPLOSION_PARTICLE_DENSITY);
    }

    /** @return the RTT in seconds */
    public static float ping(DataInputStream input, DataOutputStream output) throws IOException {
        output.write(MessageType.PING.ordinal());
        output.flush();
        long start = System.nanoTime();

        int reply = input.read();
        if (reply != MessageType.PONG.ordinal()) Logger.printError("unexpected reply: " + MessageType.get(reply));

        int deltaNanos = (int) (System.nanoTime() - start);
        return deltaNanos * 1E-9f;
    }

    /**
     * reacts on an expected ping message
     */
    private static void pong(DataInputStream input, DataOutputStream output) throws IOException {
        int ping = input.read();
        if (ping != MessageType.PING.ordinal()) throw new IOException("unexpected reply: " + ping);
        output.write(MessageType.PONG.ordinal());
        output.flush();
    }

    public static void entityRemoveSend(DataOutputStream output, int id) throws IOException {
        output.writeInt(id);
    }

    public static int entityRemoveRead(DataInputStream input) throws IOException {
        return input.readInt();
    }

    public static void raceProgressSend(DataOutputStream output, String playerID, int checkPointNr, int roundNr) throws IOException {
        output.writeUTF(playerID);
        output.writeInt(checkPointNr);
        output.writeInt(roundNr);
    }

    public static void raceProgressRead(DataInputStream input, RaceProgress progress) throws IOException {
        String playerName = input.readUTF();
        int checkPointNr = input.readInt();
        int roundNr = input.readInt();

        progress.setState(playerName, checkPointNr, roundNr);
    }

    public static void worldSwitchSend(DataOutputStream input, WorldClass world) throws IOException {
        throw new UnsupportedOperationException();
    }
}
