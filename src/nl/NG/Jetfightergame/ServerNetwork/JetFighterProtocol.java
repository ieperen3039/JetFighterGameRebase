package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Prentity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.*;
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
import java.util.function.BiConsumer;

/**
 * @author Geert van Ieperen created on 9-5-2018.
 */
public final class JetFighterProtocol {

    /**
     * writes the given entity to the DataOutputStream.
     * @see #entityUpdateRead(DataInputStream, Environment)
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
     */
    public static MovingEntity entityUpdateRead(DataInputStream input, Environment entities) throws IOException {
        // identity and time
        int id = input.readInt();
        float time = input.readFloat();
        // state
        PosVector pos = DataIO.readPosVector(input);
        Quaternionf rot = DataIO.readQuaternion(input);
        // set the entity state
        MovingEntity target = entities.getEntity(id);
        if (target == null) {
            Logger.printError("Entity with id " + id + " not found");
            return null;
        }

        target.addStatePoint(time, pos, rot);
        return target;
    }

    /** server sending a new entity */
    public static void newEntitySend(DataOutputStream output, Prentity entity, int id) throws IOException {
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
     * @return a pair with on left the name of this player, and on right the jet of this player
     * @see #playerSpawnRequest(DataOutputStream, DataInputStream, String, EntityClass, Controller, SpawnReceiver)
     */
    public static Pair<String, AbstractJet> playerSpawnAccept(
            DataInputStream input, DataOutputStream output, MovingEntity.State position,
            SpawnReceiver server, Controller controls, BiConsumer<Prentity, Integer> others
    ) throws IOException {
        EntityClass type = EntityClass.get(input.read());
        String name = input.readUTF();

        Prentity prentity = new Prentity(type, position);
        MovingEntity construct = prentity.construct(server, controls);
        assert construct instanceof AbstractJet : "player tried flying on something that is not a jet.";
        JetFighterProtocol.newEntitySend(output, prentity, construct.idNumber());
        output.flush();

        others.accept(prentity, construct.idNumber());
        return new Pair<>(name, (AbstractJet) construct);
    }


    /**
     * //TODO accept boolean from server, and handling if spawn is denied requests and receives a new jet from the
     * server, based on the given type
     * @param playerName the name you wish to use. Must be unique for this server
     * @param type       the entity you wish to fly. Should be an AbstractJet.
     * @param controls   the controls used for the resulting jet
     * @param deposit    the deposit for new entities
     * @return the jet received from the server. Not necessarily the one requested.
     */
    public static AbstractJet playerSpawnRequest(
            DataOutputStream output, DataInputStream input, String playerName, EntityClass type,
            Controller controls, SpawnReceiver deposit
    ) throws IOException {
        output.write(type.ordinal());
        output.writeUTF(playerName);
        output.flush();

        return (AbstractJet) JetFighterProtocol.newEntityRead(
                input, deposit, controls
        );
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
     * @see #explosionRead(DataInputStream, EnvironmentManager)  */
    public static void explosionSend(DataOutputStream output, PosVector position, DirVector direction, float spread, Color4f color1, Color4f color2) throws IOException {
        DataIO.writeVector(output, position);
        DataIO.writeVector(output, direction);
        output.writeFloat(spread);
        DataIO.writeColor(output, color1);
        DataIO.writeColor(output, color2);
    }

    /** reads an explosion off the DataInputStream
     * @see #explosionSend(DataOutputStream, PosVector, DirVector, float, Color4f, Color4f)  */
    public static void explosionRead(DataInputStream serverIn, EnvironmentManager game) throws IOException {
        PosVector position = DataIO.readPosVector(serverIn);
        DirVector direction = DataIO.readDirVector(serverIn);
        float power = serverIn.readFloat();
        Color4f color1 = DataIO.readColor(serverIn);
        Color4f color2 = DataIO.readColor(serverIn);

        ParticleCloud cloud = Particles.explosion(position, direction, color1, color2, power, ClientSettings.EXPLOSION_PARTICLE_DENSITY);
        game.addParticles(cloud);
    }

    /** @return the RTT in seconds */
    public static float ping(DataInputStream input, DataOutputStream output) throws IOException {
        output.write(MessageType.PING.ordinal());
        output.flush();
        long start = System.nanoTime();

        int reply = input.read();
        if (reply != MessageType.PONG.ordinal())
            Logger.printError("Unexpected reply on " + MessageType.PONG + ": " + MessageType.asString(reply));

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

    public static void entityRemoveSend(DataOutputStream output, MovingEntity id) throws IOException {
        output.writeInt(id.idNumber());
    }

    public static MovingEntity entityRemoveRead(DataInputStream input, Environment game) throws IOException {
        return game.getEntity(input.readInt());
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
        Logger.print(String.format("Player checkpoint: %s -> (%d, %d)", playerName, checkPointNr, roundNr));
    }

    public static EnvironmentClass worldSwitchRead(DataInputStream input) throws IOException {
        return EnvironmentClass.get(input.read());
    }

    public static void worldSwitchSend(DataOutputStream output, EnvironmentClass world) throws IOException {
        output.write(world.ordinal());
    }
}
