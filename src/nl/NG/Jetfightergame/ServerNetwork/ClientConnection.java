package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.ClientControl;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.*;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static nl.NG.Jetfightergame.Controllers.ControllerManager.ControllerImpl.EmptyController;
import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener, SpawnReceiver, ClientControl {
    private final DataOutputStream serverOut;
    private final DataInputStream serverIn;
    private final EnvironmentManager game;

    private Lock sendLock = new ReentrantLock();
    private GameTimer time;

    private final SubControl input;
    private final AbstractJet jet;
    private RaceProgress gameProgress;
    private String name;

    public ClientConnection(String name, OutputStream sendChannel, InputStream receiveChannel) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false);
        this.serverOut = new DataOutputStream(new BufferedOutputStream(sendChannel));
        this.serverIn = new DataInputStream(receiveChannel);
        this.game = new EnvironmentManager(false);
        this.name = name;
        this.input = new SubControl(EmptyController);
        this.gameProgress = new RaceProgress();
        game.setContext(this, gameProgress);

        // wait for confirmation of connection
        int reply = serverIn.read();
        if (reply != MessageType.CONFIRM_CONNECTION.ordinal())
            throw new IOException("Received " + MessageType.get(reply) + " as reaction on connection");

        this.time = JetFighterProtocol.syncTimerTarget(serverIn, serverOut);
        JetFighterProtocol.worldSwitchRead(serverIn, game);
        this.jet = JetFighterProtocol.playerSpawnRequest(serverOut, serverIn, name, EntityClass.BASIC_JET, input, this);

        game.addEntity(jet);
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(serverIn.read());

        switch (type) {
            case ENTITY_SPAWN:
                MovingEntity newEntity = JetFighterProtocol.newEntityRead(serverIn, this, Controller.EMPTY);
                game.addEntity(newEntity);
                break;

            case ENTITY_UPDATE:
                JetFighterProtocol.entityUpdateRead(serverIn, game);
                break;

            case ENTITY_REMOVE:
                MovingEntity entity = JetFighterProtocol.entityRemoveRead(serverIn, game);
                game.removeEntity(entity);

                if (entity instanceof TemporalEntity) {
                    ParticleCloud explosion = ((TemporalEntity) entity).explode();
                    game.addParticles(explosion);
                }
                break;

            case PLAYER_SPAWN:
                Player newPlayer = JetFighterProtocol.playerSpawnRead(serverIn, game);
                gameProgress.addPlayer(newPlayer);
                break;

            case RACE_PROGRESS:
                JetFighterProtocol.raceProgressRead(serverIn, gameProgress);
                break;

            case EXPLOSION_SPAWN:
                JetFighterProtocol.explosionRead(serverIn, game);
                break;

            case WORLD_SWITCH:
                gameProgress = new RaceProgress(gameProgress);
                game.setContext(this, gameProgress);
                JetFighterProtocol.worldSwitchRead(serverIn, game);
                break;

            case SHUTDOWN_GAME:
                /* triggers {@link #cleanup()}*/
                stopLoop();
                break;

            case CONNECTION_CLOSE:
                return false;

            default:
                Logger.print("Inappropriate message: " + type);
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
    public void addSpawn(Spawn spawn) {
        Logger.printError("client spawned his own entity (" + spawn + ")");
        game.addEntity(spawn.construct(this, null));
    }

    /**
     * sends a single control message to the server
     * @param type  the control to adapt
     * @param value the value of this message
     * @throws IOException              if there is a problem with the connection to the server
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
    protected void update(float deltaTime) throws Exception {
        Controller input = getInput();

        sendLock.lock();
        try {
            if (!input.isActiveController()) {
                // axis controls
                send(serverOut, THROTTLE, input.throttle());
                send(serverOut, PITCH, input.pitch());
                send(serverOut, YAW, input.yaw());
                send(serverOut, ROLL, input.roll());
                // binary controls
                send(serverOut, PRIMARY_FIRE, input.primaryFire());
                send(serverOut, SECONDARY_FIRE, input.secondaryFire());

            }

            serverOut.flush();
        } finally {
            sendLock.unlock();
        }
    }

    private static void send(DataOutputStream serverOut, MessageType type, boolean isEnabled) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(isEnabled);
        serverOut.write(type.ordinal());
        JetFighterProtocol.controlSend(serverOut, asByte);
    }

    private static void send(DataOutputStream serverOut, MessageType type, float value) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(value);
        serverOut.write(type.ordinal());
        JetFighterProtocol.controlSend(serverOut, asByte);
    }

    @Override
    public void stopLoop() {
        sendLock.lock();
        try {
            serverOut.write(CONNECTION_CLOSE.ordinal());
            serverOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
            cleanup();

        } finally {
            sendLock.unlock();
        }

        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        try {
            serverIn.close();
            Logger.print(this + " connection is closed");
        } catch (IOException ex1) {
            try {
                serverOut.close();
            } catch (IOException ex2) {
                ex2.addSuppressed(ex1);
                ex2.printStackTrace();
            }
        }
    }

    @Override
    public void setControl(boolean enabled) {
        if (enabled) {
            input.enable();
        } else {
            input.disable();
        }
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, Color4f color1, Color4f color2, float power, int density) {
        game.addParticles(Particles.explosion(position, direction, color1, color2, power, density));
    }

    @Override
    public Controller getInput() {
        return input;
    }

    @Override
    public ControllerManager getInputControl() {
        return input;
    }

    @Override
    public AbstractJet jet() {
        return jet;
    }

    public RaceProgress getRaceProgress() {
        return gameProgress;
    }

    @Override
    public String playerName() {
        return name;
    }

    public Environment getWorld() {
        return game;
    }

    class SubControl extends ControllerManager {
        private final ControllerImpl secondary;
        ControllerImpl active;

        public SubControl(ControllerImpl secondary) {
            super(null, ClientConnection.this);
            switchTo(0);
            this.secondary = secondary;
        }

        @Override
        public void switchTo(ControllerImpl type) {
            active = type;
            super.switchTo(type);
        }

        public void disable() {
            super.switchTo(secondary);
        }

        public void enable() {
            switchTo(active);
        }
    }

    /** a representation of players other than this player (to be used client-side) */
    public static class OtherPlayer implements Player {
        private AbstractJet jet;
        private String name;

        OtherPlayer(AbstractJet jet, String name) {
            this.jet = jet;
            this.name = name;
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
