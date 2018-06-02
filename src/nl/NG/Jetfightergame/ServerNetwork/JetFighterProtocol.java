package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Particles.DataIO;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.*;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 9-5-2018.
 */
public final class JetFighterProtocol {

    /**
     * writes the given entity to the OutputStream.
     * @see #entityUpdateRead(InputStream, Collection)
     */
    public static void entityUpdateSend(OutputStream output, MovingEntity thing, float currentTime) throws IOException {
        DataOutputStream DOS = new DataOutputStream(output);
        // identity and time
        DOS.writeInt(thing.idNumber());
        DOS.writeFloat(currentTime);
        // state
        DataIO.writeVector(DOS, thing.getPosition());
        DataIO.writeQuaternion(DOS, thing.getRotation());
    }

    /**
     * read an entity from the InputStream, match the entity with one in the list, and updates it
     * @param input    the input stream, with an entity state on its next read chunk.
     * @param entities a list of all entities of which one of them must be the one on the stream
     * @return the relevant entity, or null if it was not found
     * @throws IOException if anything goes wrong with the connection
     * @see #entityUpdateRead(InputStream, Collection)
     */
    public static MovingEntity entityUpdateRead(InputStream input, Collection<MovingEntity> entities) throws IOException {
        DataInputStream DIS = new DataInputStream(input);
        // identity and time
        int id = DIS.readInt();
        float time = DIS.readFloat();
        // state
        PosVector pos = DataIO.readPosVector(DIS);
        Quaternionf rot = DataIO.readQuaternion(DIS);
        // set the entity state
        MovingEntity target = matchEntity(entities, id);

        if (target == null) {
            Logger.printError("Entity with id " + id + " not found among " + entities.size() + " entities.");
            return null;
        }

        target.addStatePoint(time, pos, rot);
        return target;
    }

    /** returns the (first) entity with the given id, or null when it is not found */
    private static MovingEntity matchEntity(Collection<MovingEntity> entities, int id) {
        for (MovingEntity entity : entities) {
            if (entity.idNumber() == id) {
                return entity;
            }
        }

        return null;
    }

    /**
     * client sending a request of spawing a new entity.
     * @see #spawnRequestRead(InputStream)
     */
    public static void spawnRequestSend(OutputStream output, MovingEntity.Spawn spawn) throws IOException {
        output.write(spawn.type.ordinal());
        DataOutputStream DOS = new DataOutputStream(output);
        // state
        DataIO.writeVector(DOS, spawn.position);
        DataIO.writeQuaternion(DOS, spawn.rotation);
        DataIO.writeVector(DOS, spawn.velocity);
    }

    /**
     * server reading an entity from the InputStream
     * @return a description of a new entity to be added to the server
     * @see #spawnRequestSend(OutputStream, MovingEntity.Spawn)
     */
    public static MovingEntity.Spawn spawnRequestRead(InputStream input) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        DataInputStream DIS = new DataInputStream(input);
        // state
        PosVector position = DataIO.readPosVector(DIS);
        Quaternionf rotation = DataIO.readQuaternion(DIS);
        DirVector velocity = DataIO.readDirVector(DIS);

        return new MovingEntity.Spawn(type, position, rotation, velocity);
    }

    /** server sending a new entity */
    public static void newEntitySend(OutputStream output, MovingEntity.Spawn entity, int id) throws IOException {
        output.write(entity.type.ordinal());
        DataOutputStream DOS = new DataOutputStream(output);
        // identity number
        DOS.writeInt(id);
        // state
        DataIO.writeVector(DOS, entity.position);
        DataIO.writeQuaternion(DOS, entity.rotation);
        DataIO.writeVector(DOS, entity.velocity);
    }

    /** client reading an entity off the InputStream and creates an instance of it */
    public static MovingEntity newEntityRead(InputStream input, SpawnReceiver world, Controller controller) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        DataInputStream DIS = new DataInputStream(input);
        // identity number
        int id = DIS.readInt();
        // state
        PosVector position = DataIO.readPosVector(DIS);
        Quaternionf rotation = DataIO.readQuaternion(DIS);
        DirVector velocity = DataIO.readDirVector(DIS);

        return type.construct(id, world, controller, position, rotation, velocity);
    }

    /** read a control message off the InputStream */
    static void controlRead(InputStream clientIn, RemoteControlReceiver controls, MessageType type) throws IOException {
        int value = clientIn.read();
        controls.receive(type, value);
    }

    /** sends a control message into the OutputStream */
    static synchronized void controlSend(OutputStream output, byte value) throws IOException {
        output.write(value);
    }

    /**
     * sends a request to spawn a new plane. When successful, the reply is caught with {@link
     * #newEntityRead(InputStream, SpawnReceiver, Controller)}
     * @see #playerSpawnAccept(InputStream)
     */
    public static void playerSpawnRequest(OutputStream output, EntityClass entity) throws IOException {
        output.write(entity.ordinal());
    }

    /** @see #playerSpawnRequest(OutputStream, EntityClass) */
    public static EntityClass playerSpawnAccept(InputStream clientIn) throws IOException {
        return EntityClass.get(clientIn.read());
    }

    /**
     * sets up synchronizing time across server-client connection
     * @param serverTime current time according to the source
     */
    public static void syncTimerSource(InputStream input, OutputStream output, GameTimer serverTime) throws IOException {
        float deltaNanos = ping(input, output);

        DataOutputStream DOS = new DataOutputStream(output);
        DOS.writeFloat(serverTime.time() + deltaNanos);
        DOS.flush();
    }

    /**
     * updates the timer to sourceTime + ping
     */
    public static void syncTimerTarget(InputStream input, OutputStream output, GameTimer timer) throws IOException {
        // wait for signal to arrive, to let the source measure the delay
        // repeat and take average for more accurate results
        pong(input, output);

        DataInputStream DIS = new DataInputStream(input);
        float serverTime = DIS.readFloat();
        timer.set(serverTime);
    }

    /** sends an explosion or other effect to the client
     * @see #explosionRead(InputStream)  */
    public static void explosionSend(OutputStream output, PosVector position, DirVector direction, float spread, Color4f color1, Color4f color2) throws IOException {
        DataOutputStream DOS = new DataOutputStream(output);
        DataIO.writeVector(DOS, position);
        DataIO.writeVector(DOS, direction);
        DOS.writeFloat(spread);
        DataIO.writeColor(DOS, color1);
        DataIO.writeColor(DOS, color2);
    }

    /** reads an explosion off the InputStream
     * @see #explosionSend(OutputStream, PosVector, DirVector, float, Color4f, Color4f)  */
    public static ParticleCloud explosionRead(InputStream input) throws IOException {
        DataInputStream DIS = new DataInputStream(input);
        PosVector position = DataIO.readPosVector(DIS);
        DirVector direction = DataIO.readDirVector(DIS);
        float power = DIS.readFloat();
        Color4f color1 = DataIO.readColor(DIS);
        Color4f color2 = DataIO.readColor(DIS);

        return Particles.explosion(position, direction, color1, color2, power, ClientSettings.EXPLOSION_PARTICLE_DENSITY);
    }

    /** @return the RTT in seconds */
    public static float ping(InputStream input, OutputStream output) throws IOException {
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
    private static void pong(InputStream input, OutputStream output) throws IOException {
        int ping = input.read();
        if (ping != MessageType.PING.ordinal()) throw new IOException("unexpected reply: " + ping);
        output.write(MessageType.PONG.ordinal());
        output.flush();
    }

    public static void entityRemoveSend(OutputStream output, int id) throws IOException {
        DataOutputStream DOS = new DataOutputStream(output);
        DOS.writeInt(id);
    }

    public static int entityRemoveRead(InputStream input) throws IOException {
        DataInputStream DIS = new DataInputStream(input);
        return DIS.readInt();
    }
}
