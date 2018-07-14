package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.*;
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static nl.NG.Jetfightergame.Controllers.ControllerManager.ControllerImpl.EmptyController;
import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener, SpawnReceiver, ClientControl {
    private final OutputStream serverOut;
    private final InputStream serverIn;
    private final EnvironmentManager game;
    private final JetFighterProtocol protocol;

    private Lock sendLock = new ReentrantLock();
    private GameTimer time;

    private final SubControl input;
    private final AbstractJet jet;
    private RaceProgress gameProgress;
    private String name;
    private PowerupType currentPowerup = null;

    public ClientConnection(String name, OutputStream sendChannel, InputStream receiveChannel) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false);
        this.serverOut = new BufferedOutputStream(sendChannel);
        this.serverIn = receiveChannel;
        this.name = name;
        this.input = new SubControl(EmptyController);
        this.gameProgress = new RaceProgress();
        this.game = new EnvironmentManager(null, this, gameProgress, false);
        game.build();
        gameProgress.addPlayer(this);

        this.protocol = new JetFighterProtocol(serverIn, serverOut);
        this.time = protocol.syncTimerTarget();
        EnvironmentClass type = protocol.worldSwitchRead();
        game.switchTo(type);

        this.jet = protocol.playerSpawnRequest(name, EntityClass.BASIC_JET, input, this);
        game.addEntity(jet);
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type = MessageType.get(serverIn.read());

        switch (type) {
            case ENTITY_SPAWN:
                MovingEntity newEntity = protocol.newEntityRead(this, Controller.EMPTY);
                game.addEntity(newEntity);
                break;

            case ENTITY_UPDATE:
                protocol.entityUpdateRead(game);
                break;

            case ENTITY_REMOVE:
                MovingEntity entity = protocol.entityRemoveRead(game);
                game.removeEntity(entity);

                if (entity instanceof TemporalEntity) {
                    ParticleCloud explosion = ((TemporalEntity) entity).explode();
                    game.addParticles(explosion);
                }
                break;

            case PLAYER_SPAWN:
                Player newPlayer = protocol.playerSpawnRead(game);
                gameProgress.addPlayer(newPlayer);
                break;

            case RACE_PROGRESS:
                protocol.raceProgressRead(gameProgress);
                break;

            case POWERUP_COLLECT:
                addPowerup(protocol.powerupCollectRead());

            case EXPLOSION_SPAWN:
                protocol.explosionRead(game);
                break;

            case WORLD_SWITCH:
                gameProgress = new RaceProgress(gameProgress);
                game.setContext(this, gameProgress);
                EnvironmentClass world = protocol.worldSwitchRead();
                game.switchTo(world);

                gameProgress.players()
                        .stream()
                        .map(Player::jet)
                        .forEach(game::addEntity);
                break;

            case SHUTDOWN_GAME:
                stopLoop();
                return false;

            case CONNECTION_CLOSE:
                Logger.print("Received CONNECTION_CLOSE");
                return false;

            default:
                Logger.print("Inappropriate message: " + type);
        }

        return true;
    }

    /**
     * sends a single command to the server and flushes
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
    public void addSpawn(Prentity prentity) {
        Logger.printError("Client added an entity to its own world entity (" + prentity + ")");
        game.addEntity(prentity.construct(this, null));
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
            protocol.controlSend(value);
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
                send(THROTTLE, input.throttle());
                send(PITCH, input.pitch());
                send(YAW, input.yaw());
                send(ROLL, input.roll());
                // binary controls
                send(PRIMARY_FIRE, input.primaryFire());
                send(SECONDARY_FIRE, input.secondaryFire());
            }

            serverOut.flush();
        } finally {
            sendLock.unlock();
        }
    }

    private void send(MessageType type, boolean isEnabled) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(isEnabled);
        serverOut.write(type.ordinal());
        protocol.controlSend(asByte);
    }

    private void send(MessageType type, float value) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(value);
        serverOut.write(type.ordinal());
        protocol.controlSend(asByte);
    }

    @Override
    protected void cleanup() {
        try {
            serverIn.close();
            serverOut.close();
            Logger.print(this + " connection is closed");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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

    @Override
    public PowerupType getCurrentPowerup() {
        return currentPowerup;
    }

    @Override
    public boolean addPowerup(PowerupType.Primitive type) {
        PowerupType next = currentWith(type);
        if (next == null || next == currentPowerup) return false;
        currentPowerup = next;
        return true;
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
        private PowerupType currentPowerup = null;
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

        @Override
        public PowerupType getCurrentPowerup() {
            return currentPowerup;
        }

        @Override
        public boolean addPowerup(PowerupType.Primitive type) {
            PowerupType next = currentWith(type);
            if (next == currentPowerup) return false;
            currentPowerup = next;
            return true;
        }
    }
}
