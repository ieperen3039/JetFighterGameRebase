package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.EntityState;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
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

import java.io.*;
import java.util.function.BiConsumer;

/**
 *
 * @author Geert van Ieperen created on 9-5-2018.
 */
public class JetFighterProtocol {
    private static final int versionNumber = 2;

    private final DataInputStream input;
    private final DataOutputStream output;

    /**
     * creates and connects to the other side. Blocks until the protocol on the other side has also been initialized.
     * @param in  the incoming data sent from the other side
     * @param out the outgoing data to the other side
     * @throws IOException if the other side runs a different protocol version
     */
    public JetFighterProtocol(InputStream in, OutputStream out) throws IOException {
        this.output = new DataOutputStream(out);
        output.writeInt(versionNumber);
        output.flush();

        this.input = new DataInputStream(in);
        int reply = input.readInt();

        if (reply != versionNumber)
            throw new IOException("connected client has version " + reply + " and we have " + versionNumber);
    }

    /**
     * writes the given entity to the DataOutputStream.
     * @see #entityUpdateRead(Environment)
     */
    public void entityUpdateSend(MovingEntity thing, float currentTime) throws IOException {
        // identity and time
        output.writeInt(thing.idNumber());
        output.writeFloat(currentTime);
        // state
        DataIO.writeVector(output, thing.getPosition());
        DataIO.writeQuaternion(output, thing.getRotation());
    }

    /**
     * read an entity from the DataInputStream, match the entity with one in the list, and updates it
     * @param entities a list of all entities of which one of them must be the one on the stream
     * @return the relevant entity, or null if it was not found
     * @throws IOException if anything goes wrong with the connection
     */
    public MovingEntity entityUpdateRead(Environment entities) throws IOException {
        // identity and time
        int id = input.readInt();
        float time = input.readFloat();
        // state
        PosVector pos = DataIO.readPosVector(input);
        Quaternionf rot = DataIO.readQuaternion(input);
        // set the entity state
        MovingEntity target = entities.getEntity(id);
        if (target == null) {
            Logger.ERROR.print("Entity with id " + id + " not found");
            return null;
        }

        target.addStatePoint(time, pos, rot);
        return target;
    }

    /** server sending a new entity */
    public void newEntitySend(EntityFactory entity) throws IOException {
        entity.writeFactory(output);
    }

    /** client reading an entity off the DataInputStream and creates an instance of it */
    public MovingEntity newEntityRead(SpawnReceiver world, EntityMapping entities) throws IOException {
        EntityFactory pre = EntityFactory.readFactory(input);
        return pre.construct(world, entities);
    }

    /** read a control message off the DataInputStream */
    public void controlRead(RemoteControlReceiver controls, MessageType type) throws IOException {
        int value = input.read();
        controls.receive(type, value);
    }

    /** sends a control message into the DataOutputStream */
    public synchronized void controlSend(byte value) throws IOException {
        output.write(value);
    }

    /**
     * TODO: accept boolean from server, and handling if spawn is denied requests and receives a new jet from the
     * server, based on the given type
     * @param playerName the name you wish to use. Must be unique for this server
     * @param type       the entity you wish to fly. Should be an AbstractJet.
     * @param controls   the controls used for the resulting jet
     * @param deposit    the deposit for new entities
     * @param entities
     * @return the jet received from the server. Not necessarily the one requested.
     */
    public AbstractJet playerSpawnRequest(String playerName, EntityClass type, Controller controls, SpawnReceiver deposit, EntityMapping entities) throws IOException {
        output.writeUTF(playerName);
        output.writeInt(type.ordinal());
        output.flush();

        AbstractJet jet = (AbstractJet) newEntityRead(deposit, entities);
        jet.setController(controls);
        return jet;
    }

    /**
     * @return a pair with on left the name of this player, and on right the jet of this player
     * @see #playerSpawnRequest(String, EntityClass, Controller, SpawnReceiver, EntityMapping)
     */
    public Pair<String, AbstractJet> playerSpawnAccept(
            EntityState spawnState, SpawnReceiver server, Controller controls,
            BiConsumer<EntityFactory, Integer> others, EntityMapping entities
    ) throws IOException {

        String name = input.readUTF();
        EntityClass type = EntityClass.get(input.readInt());
        EntityFactory factory = EntityFactory.newFactoryOf(type, spawnState, 0);

        MovingEntity construct = factory.construct(server, entities);
        assert construct instanceof AbstractJet : "player tried flying on something that is not a jet.";
        AbstractJet jet = (AbstractJet) construct;
        jet.setController(controls);

        newEntitySend(factory);
        output.flush();

        others.accept(factory, jet.idNumber());
        return new Pair<>(name, jet);
    }

    /**
     * sends spawning of a player different from this player
     * @param world entities generated by this player are provided here
     */
    public Player playerSpawnRead(EntityMapping world) throws IOException {
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
    public void playerSpawnSend(String name, MovingEntity entity) throws IOException {
        output.write(entity.idNumber());
        output.writeUTF(name);
    }

    /**
     * sets up synchronizing time across server-client connection
     * @param serverTime current time according to the source
     */
    public void syncTimerSource(GameTimer serverTime) throws IOException {
        float deltaNanos = ping();

        output.writeFloat(serverTime.time() + deltaNanos);
        output.flush();
    }

    /**
     * updates the timer to sourceTime + ping
     */
    public GameTimer syncTimerTarget() throws IOException {
        // wait for signal to arrive, to let the source measure the delay
        // repeat and take average for more accurate results
        pong();

        float serverTime = input.readFloat();
        return new GameTimer(serverTime);
    }

    /** sends an explosion or other effect to the client
     * @see #explosionRead(Environment)  */
    public void explosionSend(PosVector position, DirVector direction, float spread, int density, Color4f color1, Color4f color2, float lingerTime, float particleSize) throws IOException {
        DataIO.writeVector(output, position);
        DataIO.writeVector(output, direction);
        output.writeFloat(spread);
        output.writeInt(density);
        DataIO.writeColor(output, color1);
        DataIO.writeColor(output, color2);
        output.writeFloat(lingerTime);
        output.writeFloat(particleSize);
    }

    /** reads an explosion off the DataInputStream
     * @see #explosionSend(PosVector, DirVector, float, int, Color4f, Color4f, float, float)  */
    public void explosionRead(Environment game) throws IOException {
        PosVector position = DataIO.readPosVector(input);
        DirVector direction = DataIO.readDirVector(input);
        float power = input.readFloat();
        int density = input.readInt();
        Color4f color1 = DataIO.readColor(input);
        Color4f color2 = DataIO.readColor(input);
        float lingerTime = input.readFloat();
        float particleSize = input.readFloat();

        if (density == 0) density = ClientSettings.EXPLOSION_PARTICLE_DENSITY;

        ParticleCloud cloud = Particles.explosion(position, direction, color1, color2, power, density, lingerTime, particleSize);
        game.addParticles(cloud);
    }

    /** @return the RTT in seconds */
    public float ping() throws IOException {
        output.flush();
        output.write(MessageType.PING.ordinal());
        output.flush();
        long start = System.nanoTime();

        int reply = input.read();
        if (reply != MessageType.PONG.ordinal())
            Logger.ERROR.print("Unexpected reply on " + MessageType.PONG + ": " + MessageType.asString(reply));

        int deltaNanos = (int) (System.nanoTime() - start);
        return deltaNanos * 1E-9f;
    }

    /**
     * reacts on an expected ping message
     */
    public void pong() throws IOException {
        int ping = input.read();
        if (ping != MessageType.PING.ordinal()) throw new IOException("unexpected reply: " + ping);
        output.write(MessageType.PONG.ordinal());
        output.flush();
    }

    public void entityRemoveSend(MovingEntity id) throws IOException {
        output.writeInt(id.idNumber());
    }

    public MovingEntity entityRemoveRead(Environment game) throws IOException {
        return game.getEntity(input.readInt());
    }

    public void raceProgressSend(String playerID, int checkPointNr, int roundNr) throws IOException {
        output.writeUTF(playerID);
        output.writeInt(checkPointNr);
        output.writeInt(roundNr);
    }

    public void raceProgressRead(RaceProgress progress) throws IOException {
        String playerName = input.readUTF();
        int checkPointNr = input.readInt();
        int roundNr = input.readInt();

        progress.setState(playerName, checkPointNr, roundNr);
    }

    public void worldSwitchSend(EnvironmentClass world) throws IOException {
        output.write(world.ordinal());
    }

    public EnvironmentClass worldSwitchRead() throws IOException {
        return EnvironmentClass.get(input.read());
    }

    public void powerupUpdateSend(PowerupEntity powerup, float collectionTime, boolean isCollected) throws IOException {
        output.writeInt(powerup.idNumber());
        output.writeFloat(collectionTime);
        output.writeBoolean(isCollected);
    }

    public void powerupUpdateRead(EntityMapping world) throws IOException {
        int id = input.readInt();
        float time = input.readFloat();
        boolean isCollected = input.readBoolean();

        MovingEntity entity = world.getEntity(id);
        assert entity instanceof PowerupEntity : String.format("Entity with id %d was supposed to be a powerup, was %s", id, entity);
        PowerupEntity powerup = (PowerupEntity) entity;

        powerup.setState(isCollected, time);
    }

    public void powerupCollectSend(PowerupType type) throws IOException {
        output.write(type.ordinal());
    }

    public PowerupType powerupCollectRead() throws IOException {
        return PowerupType.get(input.read());
    }
}
