package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.GameState.*;
import nl.NG.Jetfightergame.Rendering.Particles.DataIO;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.CountDownTimer;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.*;

import static nl.NG.Jetfightergame.Settings.ClientSettings.PARTICLE_MODIFIER;

/**
 *
 * @author Geert van Ieperen created on 9-5-2018.
 */
public class JetFighterProtocol {
    public static final int versionNumber = 8;
    private static final byte TIMER_SYNC_PINGS = 10;

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
        StatusCode.OK.write(output);
        output.flush();

        this.input = new DataInputStream(in);
        int reply = input.readInt();

        if (reply != versionNumber)
            throw new IOException("connected client has version " + reply + " and we have " + versionNumber);

        StatusCode.check(input);
    }

    /**
     * creates a protocol for reading from / writing to a file
     * @param file  the file to write to
     * @param write if true, allow writing, if false, allow reading
     * @throws FileNotFoundException if the file does not exist
     */
    public JetFighterProtocol(File file, boolean write) throws IOException {
        if (write) {
            output = new DataOutputStream(new FileOutputStream(file));
            input = new DataInputStream(System.in);
            output.writeInt(versionNumber);

        } else {
            output = new DataOutputStream(System.out);
            input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            int fileVersion = input.readInt();
            if (fileVersion != versionNumber)
                throw new IOException("File has version " + fileVersion + " and we have " + versionNumber);
        }
    }

    /**
     * alternative to creating a new protocol instance. Replies with a denied connection
     * @param out
     */
    public static void denyConnect(OutputStream out) throws IOException {
        new DataOutputStream(out).writeInt(versionNumber);
        StatusCode.DENIED.write(out);
        out.flush();
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
     * @return the associated time, or 0 if the entity is not found
     * @throws IOException if anything goes wrong with the connection
     */
    public float entityUpdateRead(Environment entities) throws IOException {
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
            return 0;
        }

        target.addStatePoint(time, pos, rot);
        return time;
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
    public void controlSend(byte value) throws IOException {
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
     * @param jetColor
     * @return on left, the jet received from the server, not necessarily the one requested.
     * on right, true if this player is allowed to send control messages to the server
     */
    public Pair<AbstractJet, Boolean> playerSpawnRequest(
            String playerName, EntityClass type, Controller controls, SpawnReceiver deposit, EntityMapping entities, Color4f jetColor
    ) throws IOException {
        output.writeUTF(playerName);
        output.writeInt(type.ordinal());
        DataIO.writeColor(output, jetColor);
        output.flush();

        AbstractJet jet = (AbstractJet) newEntityRead(deposit, entities);
        boolean isAdmin = input.readBoolean();

        jet.setController(controls);
        return new Pair<>(jet, isAdmin);
    }

    /**
     * @return a pair with on left the name of this player, and on right the jet of this player
     * @see #playerSpawnRequest(String, EntityClass, Controller, SpawnReceiver, EntityMapping, Color4f)
     */
    public Pair<String, AbstractJet> playerSpawnAccept(
            EntityState spawnState, SpawnReceiver server, Controller controls,
            EntityMapping entities, boolean isAdmin
    ) throws IOException {

        String name = input.readUTF();
        EntityClass type = EntityClass.get(input.readInt());
        Color4f color = DataIO.readColor(input);
        EntityFactory factory = EntityFactory.newFactoryOf(type, spawnState, 0);

        MovingEntity construct = factory.construct(server, entities);
        if (!(construct instanceof AbstractJet)) {
            Logger.ERROR.print("player tried flying on something that is not a jet.");
            output.close();
            return null;
        }

        AbstractJet jet = (AbstractJet) construct;
        jet.setController(controls);
        jet.setJetColor(color); // required for other players to see the color

        newEntitySend(jet.getFactory());
        output.writeBoolean(isAdmin);
        output.flush();
        return new Pair<>(name, jet);
    }

    /**
     * Sends the spawn of player b, different than this player. The entity used by player b must already be sent.
     * @param player player b
     * @param pInd   the index of this player in the race
     */
    public void playerSpawnSend(Player player, int pInd) throws IOException {
        output.writeInt(player.jet().idNumber());
        output.writeUTF(player.playerName());
        output.writeInt(pInd);
    }

    /**
     * sends spawning of a player different from this player
     * @param world entities generated by this player are provided here
     */
    public NewPlayer playerSpawnRead(EntityMapping world) throws IOException {
        int id = input.readInt();
        String name = input.readUTF();
        int pInd = input.readInt();

        MovingEntity entity = world.getEntity(id);
        assert entity != null : "Entity with id " + id + " not found";
        assert entity instanceof AbstractJet : "received an non-jet entity from the server for player " + name;

        return new NewPlayer((AbstractJet) entity, name, pInd);
    }

    /**
     * sets up synchronizing time across server-client connection
     * @param serverTime current time according to the source
     */
    public void syncTimerSource(GameTimer serverTime) throws IOException {
        float avgRTT = 0;
        output.writeInt(TIMER_SYNC_PINGS);
        for (int i = 0; i < TIMER_SYNC_PINGS; i++) {
            avgRTT += ping();
        }
        avgRTT /= TIMER_SYNC_PINGS;

        output.writeFloat(serverTime.time() + avgRTT / 2);
        output.flush();
        float ping = avgRTT * 500;
        String pingString = avgRTT < 5 ? String.format("%.03f ms", ping) : (int) (ping) + " ms";
        Logger.DEBUG.print("Ping: " + pingString);
    }

    /**
     * updates the timer to sourceTime + ping
     * @param gameTimer
     */
    public void syncTimerTarget(GameTimer gameTimer) throws IOException {
        // wait for signal to arrive, to let the source measure the delay
        int timerSyncPings = input.readInt();
        for (int i = 0; i < timerSyncPings; i++) {
            pong();
        }

        float serverTime = input.readFloat();
        gameTimer.set(serverTime);
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

        if (density == 0) density = (int) (Particles.EXPLOSION_BASE_DENSITY * PARTICLE_MODIFIER);

        ParticleCloud cloud = Particles.explosion(position, direction, color1, color2, power, density, lingerTime, particleSize);
        game.addParticles(cloud);
    }

    /** @return the RTT in seconds */
    public double ping() throws IOException {
        output.flush();
        output.write(MessageType.PING.ordinal());
        output.flush();
        long start = System.nanoTime();

        int reply = input.read();
        if (reply != MessageType.PONG.ordinal())
            Logger.ERROR.print("Unexpected reply on " + MessageType.PONG + ": " + MessageType.asString(reply));

        int deltaNanos = (int) (System.nanoTime() - start);
        return deltaNanos * 1E-9d;
    }

    /**
     * reacts on an expected ping message
     */
    public void pong() throws IOException {
        int ping = input.read();
        if (ping != MessageType.PING.ordinal()) {
            throw new IOException("unexpected reply: " + MessageType.asString(ping));
        }
        output.write(MessageType.PONG.ordinal());
        output.flush();
    }

    public void entityRemoveSend(MovingEntity id) throws IOException {
        output.writeInt(id.idNumber());
    }

    public MovingEntity entityRemoveRead(Environment game) throws IOException {
        return game.getEntity(input.readInt());
    }

    public void raceProgressSend(int pInd, int checkPointNr, int roundNr) throws IOException {
        output.writeInt(pInd);
        output.writeInt(checkPointNr);
        output.writeInt(roundNr);
    }

    public void raceProgressRead(RaceProgress progress) throws IOException {
        int pInd = input.readInt();
        int checkPointNr = input.readInt();
        int roundNr = input.readInt();

        progress.setState(pInd, checkPointNr, roundNr);
    }

    public void worldSwitchSend(EnvironmentClass world, float countDown, int maxRounds) throws IOException {
        output.writeFloat(countDown);
        output.write(world.ordinal());
        output.writeInt(maxRounds);
    }

    public void worldSwitchRead(EnvironmentManager game, CountDownTimer counter, float currentTime, RaceProgress raceProgress) throws IOException {
        counter.setTime(currentTime + input.readFloat());
        EnvironmentClass world = EnvironmentClass.get(input.read());
        raceProgress.setMaxRounds(input.readInt());
        game.switchTo(world);
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

    public void sendBoosterColor(AbstractJet jet, Color4f color1, Color4f color2, float duration) throws IOException {
        output.writeInt(jet.idNumber());
        DataIO.writeColor(output, color1);
        DataIO.writeColor(output, color2);
        output.writeFloat(duration);
    }

    public void readBoosterColor(EntityMapping entities) throws IOException {
        AbstractJet jet = (AbstractJet) entities.getEntity(input.readInt());
        Color4f color1 = DataIO.readColor(input);
        Color4f color2 = DataIO.readColor(input);
        float duration = input.readFloat();
        jet.setBoosterColor(color1, color2, duration);
    }

    public void sendText(String message) throws IOException {
        output.writeUTF(message);
    }

    public String readText() throws IOException {
        return input.readUTF();
    }

    public InputStream getInput() {
        return input;
    }

    public OutputStream getOutput() {
        return output;
    }

    private enum StatusCode {
        OK("OK"), DENIED("The connection was denied");

        public final String message;

        StatusCode(String message) {
            this.message = message;
        }

        /**
         * @param in the input stream
         * @throws IllegalStateException if id is not OK.
         */
        public static void check(InputStream in) throws IllegalStateException, IOException {
            int id = in.read();
            StatusCode s = values()[id];
            if (s != OK) {
                throw new IllegalStateException(s.message);
            }
        }

        public void write(OutputStream out) throws IOException {
            out.write(ordinal());
        }
    }

    /** a representation of players other than this player (to be used client-side) */
    public static class NewPlayer implements Player {
        private AbstractJet jet;
        private String name;
        private final int pInd;

        private NewPlayer(AbstractJet jet, String name, int pInd) {
            this.jet = jet;
            this.name = name;
            this.pInd = pInd;
        }

        public void register(RaceProgress raceProgress, Player client) {
            if (name.equals(client.playerName())) {
                raceProgress.setThisPlayer(pInd);
                raceProgress.setPlayer(client, pInd);
            } else {
                raceProgress.setPlayer(this, pInd);
            }
        }

        @Override
        public String playerName() {
            return name;
        }

        @Override
        public AbstractJet jet() {
            return jet;
        }
    }
}
