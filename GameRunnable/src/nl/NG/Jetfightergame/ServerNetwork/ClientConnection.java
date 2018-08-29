package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.ControllerManager;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.CountDownTimer;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static nl.NG.Jetfightergame.Controllers.ControllerManager.ControllerImpl.AIController;
import static nl.NG.Jetfightergame.Controllers.ControllerManager.ControllerImpl.EmptyController;
import static nl.NG.Jetfightergame.ServerNetwork.MessageType.*;
import static nl.NG.Jetfightergame.Settings.ClientSettings.FIRE_PARTICLE_SIZE;

/**
 * @author Geert van Ieperen created on 6-5-2018.
 */
public class ClientConnection extends AbstractGameLoop implements BlockingListener, SpawnReceiver, ClientControl {
    private final OutputStream serverOut;
    private final InputStream serverIn;
    private final EnvironmentManager game;
    private final JetFighterProtocol protocol;
    private final boolean isAdmin;
    private final AbstractJet jet;
    private final GameTimer gameTimer;
    private final SubControl input;
    private final CountDownTimer counter;
    private final String name;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Collection<AudioSource> soundSources = new HashSet<>();

    private Lock sendLock = new ReentrantLock();
    private RaceProgress raceProgress;
    protected boolean controlTeardown = false;
    protected float maxServerTime = 0;

    public ClientConnection(String name, OutputStream sendChannel, InputStream receiveChannel, EntityClass jetType) throws IOException {
        super("Connection Controller", ClientSettings.CONNECTION_SEND_FREQUENCY, false);
        this.serverOut = new BufferedOutputStream(sendChannel);
        this.serverIn = receiveChannel;
        this.name = name;
        this.raceProgress = new RaceProgress();
        this.input = new SubControl(AIController, raceProgress);
        this.game = new EnvironmentManager(null, this, raceProgress, false, false);

        this.protocol = new JetFighterProtocol(serverIn, serverOut);
        this.gameTimer = new GameTimer();
        protocol.syncTimerTarget(gameTimer);
        this.counter = new CountDownTimer(0, gameTimer);
        protocol.worldSwitchRead(game, counter, gameTimer.time(), raceProgress);

        Pair<AbstractJet, Boolean> pair = protocol.playerSpawnRequest(name, jetType, input, this, game);
        this.isAdmin = pair.right;
        this.jet = pair.left;
        game.addEntity(jet);

        Logger.printOnline(() -> jet.getPosition() + " | " + jet.getForward());
    }

    protected ClientConnection(String name, File readFile, GameTimer timer, EntityFactory jetReplacement, int tps) throws IOException {
        super(name, tps, false);

        this.raceProgress = new RaceProgress();
        this.game = new EnvironmentManager(null, this, raceProgress, false, false);
        this.game.switchTo(EnvironmentClass.LOBBY);
        this.protocol = new JetFighterProtocol(readFile, false);
        this.input = new SubControl(EmptyController, raceProgress);
        this.gameTimer = timer;
        this.counter = new CountDownTimer(0, gameTimer);
        this.name = name;

        serverIn = protocol.getInput();
        serverOut = protocol.getOutput();
        gameTimer.set(new DataInputStream(serverIn).readFloat());

        MovingEntity construct = jetReplacement.construct(this, game);
        jet = (AbstractJet) construct;
        jet.setController(input);

        isAdmin = false;
        input.disable();
    }

    @Override
    public boolean handleMessage() throws IOException {
        MessageType type;

        try {
            type = MessageType.get(serverIn.read());
        } catch (IOException s) {
            Logger.ERROR.print(s.getMessage());
            type = MessageType.CONNECTION_CLOSE;
        }

        switch (type) {
            case CONNECTION_CLOSE:
                return false;

            case PING:
                sendCommand(PONG);
                break;

            case TEXT_MESSAGE:
                Logger.INFO.print(protocol.readText());
                break;

            case SYNC_TIMER:
                protocol.syncTimerTarget(gameTimer);
                sendLock.unlock();
                break;

            case PAUSE_GAME:
                gameTimer.pause();
                break;

            case UNPAUSE_GAME:
                gameTimer.unPause();
                startTimerSync();
                break;

            case ENTITY_SPAWN:
                MovingEntity newEntity = protocol.newEntityRead(this, game);
                game.addEntity(newEntity);
                break;

            case ENTITY_UPDATE:
                float t = protocol.entityUpdateRead(game);
                maxServerTime = Math.max(maxServerTime, t);
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
                protocol.playerSpawnRead(game).register(raceProgress, this);
                break;

            case RACE_PROGRESS:
                protocol.raceProgressRead(raceProgress);

                if (raceProgress.thisPlayerHasFinished() && !controlTeardown) {
                    controlTeardown = true;
                    input.disable();
                }

                break;

            case POWERUP_STATE:
                protocol.powerupUpdateRead(game);
                break;

            case POWERUP_COLLECT:
                jet.setPowerup(protocol.powerupCollectRead());
                break;

            case EXPLOSION_SPAWN:
                protocol.explosionRead(game);
                break;

            case BOOSTER_COLOR_CHANGE:
                protocol.readBoosterColor(game);
                break;

            case WORLD_SWITCH:
                protocol.worldSwitchRead(game, counter, gameTimer.time(), raceProgress);
                worldSwitch();
                break;

            case SHUTDOWN_GAME:
                stopLoop();
                return false;

            default:
                Logger.WARN.print("Inappropriate message: " + type);
        }

        return true;
    }

    protected void worldSwitch() {
        AudioSource.disposeAll(soundSources);
        soundSources.clear();
        soundSources.add(new AudioSource(getWorld().backgroundMusic(), ClientSettings.BACKGROUND_MUSIC_GAIN, true));

        game.addEntity(jet);
        controlTeardown = false;
        input.enable();
    }

    private void startTimerSync() throws IOException {
        sendLock.lock();
        serverOut.write(SYNC_TIMER.ordinal());
        serverOut.flush();
    }

    /**
     * sends a single command to the server and flushes
     */
    public void sendCommand(MessageType type) {
        if (type.isOf(adminOnly) && !isAdmin) return;

        sendLock.lock();
        try {
            serverOut.write(type.ordinal());
            serverOut.flush();

        } catch (IOException ex) {
            Logger.ERROR.print(ex);

        } finally {
            sendLock.unlock();
        }
    }

    @Override
    public void add(EntityFactory entityFactory) {
        Logger.ERROR.print("Client added an entity to its own world (" + entityFactory + ")");
        game.addEntity(entityFactory.construct(this, game));
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
        soundSources.forEach(AudioSource::update);
        soundSources.removeIf(AudioSource::isOverdue);

        Controller input = getInput();
        if (!input.isActiveController()) {
            sendLock.lock();
            try {
                input.update();
                // axis controls
                sendControlUnsafe(THROTTLE, input.throttle());
                sendControlUnsafe(PITCH, input.pitch());
                sendControlUnsafe(YAW, input.yaw());
                sendControlUnsafe(ROLL, input.roll());
                // binary controls
                sendControlUnsafe(PRIMARY_FIRE, input.primaryFire());
                sendControlUnsafe(SECONDARY_FIRE, input.secondaryFire());

                serverOut.flush();
            } finally {
                sendLock.unlock();
            }
        }
    }

    private void sendControlUnsafe(MessageType type, boolean isEnabled) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(isEnabled);
        serverOut.write(type.ordinal());
        protocol.controlSend(asByte);
    }

    private void sendControlUnsafe(MessageType type, float value) throws IOException {
        byte asByte = RemoteControlReceiver.toByte(value);
        serverOut.write(type.ordinal());
        protocol.controlSend(asByte);
    }

    @Override
    protected void cleanup() {
        AudioSource.disposeAll(soundSources);
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
        return gameTimer;
    }

    public CountDownTimer countDownGui() {
        return counter;
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, Color4f color1, Color4f color2, float power, int density, float lingerTime, float particleSize) {
        game.addParticles(Particles.explosion(position, direction, color1, color2, power, density, lingerTime, FIRE_PARTICLE_SIZE));
    }

    @Override
    public void powerupCollect(PowerupEntity powerup, float collectionTime, boolean isCollected) {
        Logger.WARN.print("Clientside powerup collection");
    }

    @Override
    public void playerPowerupState(AbstractJet jet, PowerupType newType) {
        Logger.WARN.print("Clientside powerup collection");
    }

    @Override
    public void add(ParticleCloud particles) {
        game.addParticles(particles);
    }

    @Override
    public void add(AudioSource source) {
        soundSources.add(source);
    }

    @Override
    public void addGravitySource(Supplier<PosVector> position, float magnitude, float duration) {
    }

    @Override
    public void boosterColorChange(AbstractJet jet, Color4f color1, Color4f color2, float duration) {
        jet.setBoosterColor(color1, color2, duration);
    }

    @Override
    public boolean isHeadless() {
        return false;
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
        return raceProgress;
    }

    @Override
    public String playerName() {
        return name;
    }

    public Environment getWorld() {
        return game;
    }

    @Override
    public void pause() {
        if (isAdmin) {
            sendCommand(PAUSE_GAME);
        } else {
            setControl(false);
        }
        soundSources.forEach(AudioSource::pause);
//        NOT super.pause() to allow AI to take over
    }

    @Override
    public void unPause() {
        if (isAdmin) {
            sendCommand(UNPAUSE_GAME);
        } else {
            setControl(!controlTeardown);
        }
        soundSources.forEach(AudioSource::play);
//        NOT super.unPause() to allow AI to take over
    }

    class SubControl extends ControllerManager {
        private final ControllerImpl secondary;
        ControllerImpl active;

        public SubControl(ControllerImpl secondary, RaceProgress raceProgress) {
            super(null, ClientConnection.this, raceProgress);
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

    /** executes the action, which may throw an IOException */
    private interface IOAction {
        void run() throws IOException;
    }
}
