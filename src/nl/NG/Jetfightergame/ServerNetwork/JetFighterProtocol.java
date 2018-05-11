package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState.EntityManager;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.io.*;
import java.util.Collection;

/**
 * @author Geert van Ieperen created on 9-5-2018.
 */
public final class JetFighterProtocol {

    /** writes the given entity to the OutputStream.
     * @see #entityUpdateRead(InputStream, Collection) */
    public static void entityUpdateSend(OutputStream output, MovingEntity thing, float currentTime) throws IOException {
        DataOutputStream DOS = new DataOutputStream(output);
        // identity and time
        DOS.writeInt(thing.idNumber());
        DOS.writeFloat(currentTime);
        // state
        writeVector(DOS, thing.getPosition());
        writeQuaternion(DOS, thing.getRotation());
    }

    /**
     * read an entity from the InputStream, match the entity with one in the list, and updates it
     * @param input the input stream, with an entity state on its next read chunk.
     * @param entities a list of all entities of which one of them must be the one on the stream
     * @throws IOException if anything goes wrong with the connection
     * @see #entityUpdateRead(InputStream, Collection)
     */
    public static void entityUpdateRead(InputStream input, Collection<MovingEntity> entities) throws IOException {
        DataInputStream DIS = new DataInputStream(input);
        // identity and time
        int id = DIS.readInt();
        float time = DIS.readFloat();
        // state
        PosVector pos = readPosVector(DIS);
        Quaternionf rot = readQuaternion(DIS);
        // set the entity state
        MovingEntity target = matchEntity(entities, id);

        if (target == null) {
            Toolbox.printError("Entity with id "+ id +" not found among " + entities.size() + " entities.");
            return;
        }

        target.addPositionPoint(pos, time);
        target.addRotationPoint(rot, time);
    }

    /** returns the (first) entity with the given id, or null when it is not found */
    private static MovingEntity matchEntity(Collection<MovingEntity> entities, int id) {
        for (MovingEntity entity : entities) {
            if (entity.idNumber() == id){
                return entity;
            }
        }

        return null;
    }

    /** writes the given rotation to the given output stream */
    private static void writeQuaternion(DataOutputStream DOS, Quaternionf rot) throws IOException {
        DOS.writeFloat(rot.x);
        DOS.writeFloat(rot.y);
        DOS.writeFloat(rot.z);
        DOS.writeFloat(rot.w);
    }

    /** reads the next 4 floats on the stream as quaternion */
    private static Quaternionf readQuaternion(DataInputStream DIS) throws IOException {
        return new Quaternionf(DIS.readFloat(), DIS.readFloat(), DIS.readFloat(), DIS.readFloat());
    }

    /** writes the given vector to the given output stream */
    private static void writeVector(DataOutputStream DOS, Vector p) throws IOException {
        DOS.writeFloat(p.x);
        DOS.writeFloat(p.y);
        DOS.writeFloat(p.z);
    }

    /** reads the next 3 floats on the stream as vector */
    private static PosVector readPosVector(DataInputStream DOS) throws IOException {
        return new PosVector(DOS.readFloat(), DOS.readFloat(), DOS.readFloat());
    }

    /** reads the next 3 floats on the stream as vector */
    private static DirVector readDirVector(DataInputStream DOS) throws IOException {
        return new DirVector(DOS.readFloat(), DOS.readFloat(), DOS.readFloat());
    }

    /** client sending a request of spawing a new entity.
     * @see #spawnRequestRead(InputStream) */
    public static void spawnRequestSend(OutputStream output, EntityClass entity, PosVector position, Quaternionf rotation, DirVector velocity) throws IOException {
        output.write(entity.ordinal());
        DataOutputStream DOS = new DataOutputStream(output);
        // state
        writeVector(DOS, position);
        writeQuaternion(DOS, rotation);
        writeVector(DOS, velocity);
    }

    /**
     * server reading an entity from the InputStream
     * @return a description of a new entity to be added to the server
     * @see #spawnRequestSend(OutputStream, EntityClass, PosVector, Quaternionf, DirVector)
     */
    public static MovingEntity.SpawnEntity spawnRequestRead(InputStream input) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        DataInputStream DIS = new DataInputStream(input);
        // state
        PosVector position = readPosVector(DIS);
        Quaternionf rotation = readQuaternion(DIS);
        DirVector velocity = readDirVector(DIS);

        return new MovingEntity.SpawnEntity(type, position, rotation, velocity);
    }

    /** server sending a new entity */
    public static void newEntitySend(OutputStream output, MovingEntity.SpawnEntity entity, int id) throws IOException {
        output.write(entity.type.ordinal());
        DataOutputStream DOS = new DataOutputStream(output);
        // identity number
        DOS.writeInt(id);
        // state
        writeVector(DOS, entity.position);
        writeQuaternion(DOS, entity.rotation);
        writeVector(DOS, entity.velocity);
    }

    /** client reading an entity off the InputStream and creates an instance of it */
    public static MovingEntity newEntityRead(InputStream input, EntityManager game, Controller controller, GameTimer time) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        DataInputStream DIS = new DataInputStream(input);
        // identity number
        int id = DIS.readInt();
        // state
        PosVector position = readPosVector(DIS);
        Quaternionf rotation = readQuaternion(DIS);
        DirVector velocity = readDirVector(DIS);

        return type.construct(id, game, controller, position, rotation, velocity, time);
    }

    /** read a control message off the InputStream */
    static void controlRead(InputStream clientIn, RemoteControlReceiver controls, MessageType type) throws IOException {
        int value = clientIn.read();
        controls.receive(type, value);
    }

    /** sends a control message into the OutputStream */
    static synchronized void controlSend(OutputStream output, MessageType type, byte value) throws IOException {
        output.write(type.ordinal());
        output.write(value);
    }

    /** sends a request to spawn a new plane.
     * When successful, the reply is caught with {@link #newEntityRead(InputStream, EntityManager, Controller, GameTimer)} */
    public static void playerSpawnRequest(OutputStream output, EntityClass entity) throws IOException {
        output.write(entity.ordinal());
    }
}
