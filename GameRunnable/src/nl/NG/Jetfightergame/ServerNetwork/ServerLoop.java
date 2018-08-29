package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.ArtificalIntelligence.RaceAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetSpitsy;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.StateWriter;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.RaceProgress.RaceChangeListener;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * mostly a controlling layer between server and an environment
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ServerLoop extends AbstractGameLoop implements GameServer, RaceChangeListener {

    private final List<ServerConnection> connections;
    private final List<Player> npcPlayers;
    private final RaceProgress raceProgress;
    private final boolean makeRecording;

    private GameTimer globalTime;
    private EnvironmentManager gameWorld;
    private EnvironmentClass raceWorld;
    private final EnvironmentClass lobby;

    private boolean worldShouldSwitch = false;
    private volatile boolean allowPlayerJoin = true;
    private int maxRounds = 1;

    public ServerLoop(EnvironmentClass lobby, EnvironmentClass raceWorld, boolean makeRecording) {
        super("Server", ServerSettings.TARGET_TPS, true);
        this.raceProgress = new RaceProgress(8, this);
        this.gameWorld = new EnvironmentManager(lobby, this, raceProgress, true, true);
        this.raceWorld = raceWorld;
        this.lobby = lobby;
        this.globalTime = new GameTimer(ClientSettings.RENDER_DELAY);
        this.connections = new ArrayList<>();

        npcPlayers = getNPCPlayers(ServerSettings.NOF_FUN);

        gameWorld.build();
        this.makeRecording = makeRecording;
    }

    /**
     * generates players controlled by RaceAI
     * @param nOfPlayers the number of players generated
     * @return a list of AI controlled players
     */
    private List<Player> getNPCPlayers(int nOfPlayers) {
        List<Player> npcs = new ArrayList<>();
        // add FUN
        for (int i = 0; i < nOfPlayers; i++) {
            EntityFactory blueprint = new JetSpitsy.Factory(new EntityState(), 0);
            AbstractJet jet = (AbstractJet) blueprint.construct(this, gameWorld);

            String name = "AI-" + i;
            Player npc = new Player() {
                @Override
                public String playerName() {
                    return name;
                }

                @Override
                public AbstractJet jet() {
                    return jet;
                }
            };
            Controller controller = new RaceAI(npc, raceProgress, gameWorld);
            controller.update();
            jet.setController(controller);
            npcs.add(npc);
        }
        return npcs;
    }

    /**
     * initialize connections with the player, accepting a new jet and sending all entities to the client.
     * @param receive  the incoming communication from the player
     * @param send the outgoing communication to the player
     * @param asAdmin if true, the connection has admin capabilities
     * @throws IOException if the connection could not be established
     */
    public void connectToPlayer(InputStream receive, OutputStream send, boolean asAdmin) throws IOException {
        if (!allowPlayerJoin) {
            Logger.WARN.print("New player tried connecting, but this is disabled");
            JetFighterProtocol.denyConnect(send);
        }

        // establish communication handler
        ServerConnection player = new ServerConnection(
                receive, send, this,
                gameWorld.getNewSpawnPosition(), gameWorld.getCurrentType(), gameWorld, asAdmin
        );

        EntityFactory factory = player.jet().getFactory();
        connections.forEach(conn -> conn.sendEntitySpawn(factory));

        if (connections.contains(player)) {
            Logger.ERROR.print("Player " + player + " already exists on the server");
            player.closeConnection("That name already exists on the server");
        }

        // send all entities until this point (excluding the player's jet)
        for (MovingEntity entity : gameWorld.getEntities()) {
            player.sendEntitySpawn(entity.getFactory());
        }

        AbstractJet playerJet = player.jet();
        gameWorld.addEntity(playerJet);
        gameWorld.updateGameLoop();
        int pInd = raceProgress.addPlayer(player);
        player.sendPlayerSpawn(player, pInd);

        for (ServerConnection conn : connections) {
            if (conn instanceof StateWriter) continue;
            player.sendPlayerSpawn(conn, raceProgress.getPlayerInd(conn));
            conn.sendPlayerSpawn(player, pInd);
            conn.flush();
        }

        connections.add(player);
        player.flush();

        player.listenInThread(true);
    }

    @Override
    public void add(EntityFactory entityFactory) {
        MovingEntity entity = entityFactory.construct(this, gameWorld);
        gameWorld.addEntity(entity);
        connections.forEach(conn -> conn.sendEntitySpawn(entityFactory));
    }

    @Override
    public GameTimer getTimer() {
        return globalTime;
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, Color4f color1, Color4f color2, float power, int density, float lingerTime, float particleSize) {
        connections.forEach(conn -> conn.sendExplosionSpawn(position, direction, power, density, color1, color2, lingerTime, particleSize));
    }

    @Override
    public void powerupCollect(PowerupEntity powerup, float collectionTime, boolean isCollected) {
        connections.forEach(conn -> conn.sendPowerupUpdate(powerup, collectionTime, isCollected));
    }

    @Override
    public void playerPowerupState(AbstractJet jet, PowerupType newType) {
        for (ServerConnection player : connections) {
            if (player.jet() == jet) {
                player.sendPowerupCollect(newType);
                break;
            }
        }
    }

    @Override
    public void add(ParticleCloud particles) {
        Logger.WARN.printSpamless("ServerLoop#addParticles",
                "Tried adding particles while headless: sending just particles is not supported");
    }

    @Override
    public void add(AudioSource source) {
        Logger.WARN.printSpamless("ServerLoop#addSoundSource",
                "Tried playing a sound while running headless: " + source);
    }

    @Override
    public void addGravitySource(Supplier<PosVector> position, float magnitude, float duration) {
        gameWorld.addGravitySource(position, magnitude, globalTime.time() + duration);
    }

    @Override
    public void boosterColorChange(AbstractJet jet, Color4f color1, Color4f color2, float duration) {
        connections.forEach(c -> c.sendBoosterColorChange(jet, color1, color2, duration));
    }

    @Override
    public boolean isHeadless() {
        return true;
    }

    @Override
    protected void update(float deltaTime) {
        if (worldShouldSwitch) {
            if (gameWorld.getCurrentType() == lobby) {
                if (makeRecording) startStateWriter();
                setWorld(raceWorld, maxRounds);
                allowPlayerJoin = false;

            } else {
                setWorld(lobby, 0);
                allowPlayerJoin = true;
            }
        }

        // indexed loop for removing
        for (int i = 0; i < connections.size(); i++) {
            ServerConnection conn = connections.get(i);
            if (conn.isClosed()) {
                Logger.WARN.print("Removing " + conn.playerName() + " from the game (disconnect)");

                AbstractJet jet = conn.jet();
                if (jet != null) removeEntity(conn.jet());

                connections.remove(i--);
                if (connections.isEmpty()) stopLoop();
            }
        }

        globalTime.updateGameTime();
        Float currentTime = globalTime.getGameTime().current();
        gameWorld.updateGameLoop();

        Collection<MovingEntity> entities = gameWorld.getEntities();

        for (MovingEntity ety : entities) {
            if (ety instanceof PowerupEntity) continue;

            if (TemporalEntity.isOverdue(ety)) {
                removeEntity(ety);

            } else {
                connections.forEach(conn -> conn.sendEntityUpdate(ety, currentTime));
            }
        }

        connections.forEach(ServerConnection::flush);
    }

    private void startStateWriter() {
        try {
            StateWriter st = new StateWriter(globalTime.time());
            connections.add(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeEntity(MovingEntity ety) {
        connections.forEach(conn -> conn.sendEntityRemove(ety));
        gameWorld.removeEntity(ety);
    }

    @Override
    public void startRace() {
        worldShouldSwitch = true;
    }

    @Override
    public void pause() {
        globalTime.pause();
        connections.forEach(conn -> conn.send(MessageType.PAUSE_GAME));
        super.pause();
    }

    @Override
    public void unPause() {
        globalTime.unPause();
        connections.forEach(conn -> conn.send(MessageType.UNPAUSE_GAME));
        super.unPause();
    }

    @Override
    public void shutDown() {
        stopLoop();
    }

    @Override
    public void stopLoop() {
        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        for (ServerConnection conn : connections) {
            if (!conn.isClosed()) {
                conn.send(MessageType.SHUTDOWN_GAME);
            }
        }

        gameWorld.cleanUp();
    }

    private void setWorld(EnvironmentClass world, int maxRounds) {
        Logger.INFO.print("Switching world to " + world);

        worldShouldSwitch = false;
        float countDown = maxRounds > 0 ? ServerSettings.COUNT_DOWN : 0;

        connections.forEach(conn -> conn.sendWorldSwitch(world, countDown, maxRounds));
        raceProgress.setMaxRounds(maxRounds);
        // startup new world
        gameWorld.switchTo(world);

        // sync new world with players
        for (ServerConnection player : connections) {
            if (player instanceof StateWriter) continue;

            int pInd = raceProgress.addPlayer(player);
            AbstractJet jet = player.jet();
            jet.set(gameWorld.getNewSpawnPosition());
            jet.setPowerup(PowerupType.NONE);
            jet.addSpeedModifier(0, countDown);
            player.sendPowerupCollect(PowerupType.NONE);
            player.sendEntityUpdate(jet, globalTime.time() - 1);
            player.sendEntityUpdate(jet, globalTime.time());

            gameWorld.addEntity(jet);
            for (ServerConnection conn : connections) {
                if (conn != player) conn.sendEntitySpawn(jet.getFactory());
                conn.sendPlayerSpawn(player, pInd);
            }
        }

        // add npc players
        if (maxRounds > 0) for (Player npc : npcPlayers) {
            int pInd = raceProgress.addPlayer(npc);
            AbstractJet jet = npc.jet();
            jet.set(gameWorld.getNewSpawnPosition());
            jet.setPowerup(PowerupType.NONE);
            jet.addSpeedModifier(0, countDown);
            gameWorld.addEntity(jet);

            for (ServerConnection conn : connections) {
                conn.sendEntitySpawn(jet.getFactory());
                conn.sendPlayerSpawn(npc, pInd);
            }
        }
    }

    public void playerCheckpointUpdate(int pInd, int checkpointProgress, int roundProgress) {
        connections.forEach(conn -> conn.sendProgress(pInd, checkpointProgress, roundProgress));
    }
}
