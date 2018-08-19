package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.ArtificalIntelligence.HunterAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetBasic;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetSpitsy;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.RaceProgress.RaceChangeListener;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
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
    private final RaceProgress raceProgress;

    private EnvironmentManager gameWorld;
    private GameTimer globalTime;
    private EnvironmentClass raceWorld;
    private boolean worldShouldSwitch = false;
    private volatile boolean allowPlayerJoin = true;
    private int maxRounds = 0;

    public ServerLoop(EnvironmentClass lobby, EnvironmentClass raceWorld) {
        super("Server", ServerSettings.TARGET_TPS, true);
        this.raceProgress = new RaceProgress(8, this);
        this.gameWorld = new EnvironmentManager(lobby, this, raceProgress, true, true);
        this.raceWorld = raceWorld;
        this.globalTime = new GameTimer(ClientSettings.RENDER_DELAY);
        this.connections = new ArrayList<>();

        gameWorld.build();
        Logger.printOnline(() -> Float.toString(globalTime.time()));
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
                receive, send,
                this, (spawn, id) -> connections.forEach(conn -> conn.sendEntitySpawn(spawn)),
                gameWorld.getNewSpawnPosition(), gameWorld.getCurrentType(), gameWorld,
                asAdmin
        );

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
        gameWorld.updateGameLoop(globalTime.getGameTime().current(), globalTime.getGameTime().difference());
        int pInd = raceProgress.addPlayer(player);
        player.sendPlayerSpawn(player, pInd);

        for (ServerConnection conn : connections) {
            player.sendPlayerSpawn(conn, raceProgress.getPlayerInd(conn));
            conn.sendPlayerSpawn(player, pInd);
            conn.flush();
        }

        connections.add(player);
        player.flush();

        player.listenInThread(true);

        Logger.printOnline(() -> player.jet().interpolatedPosition().toString());
    }

    @Override
    public void addSpawn(EntityFactory entityFactory) {
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
    public void addGravitySource(Supplier<PosVector> position, float magnitude, float duration) {
        gameWorld.addGravitySource(position, magnitude, globalTime.time() + duration);
    }

    @Override
    public void boosterColorChange(AbstractJet jet, Color4f color1, Color4f color2, float duration) {
        connections.forEach(c -> c.sendBoosterColorChange(jet, color1, color2, duration));
    }

    @Override
    protected void update(float deltaTime) {
        if (worldShouldSwitch) setWorld(raceWorld, 1);

        for (ServerConnection conn : connections) {
            if (conn.isClosed()) {
                Logger.WARN.print("Removing " + conn.playerName() + " from the game (disconnect)");
                connections.remove(conn);
                removeEntity(conn.jet());

                if (connections.isEmpty()) stopLoop();
            }
        }

        globalTime.updateGameTime();
        TrackedFloat time = globalTime.getGameTime();
        gameWorld.updateGameLoop(time.current(), time.difference());

        Collection<MovingEntity> entities = gameWorld.getEntities();

        for (MovingEntity ety : entities) {
            if (ety instanceof PowerupEntity) continue;

            if (TemporalEntity.isOverdue(ety)) {
                removeEntity(ety);

            } else {
                connections.forEach(conn -> conn.sendEntityUpdate(ety, time.current()));
            }
        }
        connections.forEach(ServerConnection::flush);
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
        for (ServerConnection conn : connections) {
            if (!conn.isClosed()) {
                conn.send(MessageType.SHUTDOWN_GAME);
            }
        }

        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        gameWorld.cleanUp();
    }

    private void setWorld(EnvironmentClass world, int maxRounds) {
        Logger.INFO.print("Switching world to " + world);

        allowPlayerJoin = false;
        connections.forEach(conn -> conn.sendWorldSwitch(world, ServerSettings.COUNT_DOWN));
        this.maxRounds = maxRounds;
        // startup new world
        gameWorld.switchTo(world);

        // sync new world with players
        for (ServerConnection player : connections) {
            int pInd = raceProgress.addPlayer(player);
            AbstractJet jet = player.jet();
            jet.set(gameWorld.getNewSpawnPosition());
            jet.setPowerup(PowerupType.NONE);
            player.sendPowerupCollect(PowerupType.NONE);

            gameWorld.addEntity(jet);
            for (ServerConnection conn : connections) {
                player.sendEntityUpdate(jet, globalTime.time());
                conn.sendPlayerSpawn(player, pInd);
            }
        }

        worldShouldSwitch = false;

        // add FUN
        for (int i = 0; i < ServerSettings.NOF_FUN; i++) {
            MovingEntity target = connections.get(0).jet();
            EntityFactory blueprint = new JetSpitsy.Factory(gameWorld.getNewSpawnPosition(), 0);

            AbstractJet npc = (AbstractJet) blueprint.construct(this, gameWorld);
            String name = "AI-" + i;

            Controller controller = new HunterAI(npc, target, gameWorld, JetBasic.THROTTLE_POWER / JetBasic.AIR_RESISTANCE_COEFFICIENT);
            npc.setController(controller);
            gameWorld.addEntity(npc);

            Player asPlayer = new Player() {
                @Override
                public String playerName() {
                    return name;
                }

                @Override
                public AbstractJet jet() {
                    return npc;
                }
            };

            int pInd = raceProgress.addPlayer(asPlayer);

            for (ServerConnection conn : connections) {
                conn.sendEntitySpawn(blueprint);
                conn.sendPlayerSpawn(asPlayer, pInd);
            }
        }
    }

    public void playerCheckpointUpdate(int pInd, int checkpointProgress, int roundProgress) {
        if (roundProgress >= maxRounds) {
            connections.forEach(conn -> conn.sendProgress(pInd, raceProgress.getNumCheckpoints(), maxRounds - 1));
        } else {
            connections.forEach(conn -> conn.sendProgress(pInd, checkpointProgress, roundProgress));
        }
    }
}
