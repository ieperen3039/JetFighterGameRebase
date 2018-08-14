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

    private EnvironmentManager gameWorld;
    private GameTimer globalTime;
    private EnvironmentClass raceWorld;
    private boolean worldShouldSwitch = false;

    public ServerLoop(EnvironmentClass lobby, EnvironmentClass raceWorld) {
        super("Server", ServerSettings.TARGET_TPS, true);
        this.gameWorld = new EnvironmentManager(lobby, this, new RaceProgress(), true, 1);
        this.raceWorld = raceWorld;
        this.globalTime = new GameTimer(ClientSettings.RENDER_DELAY);
        this.connections = new ArrayList<>();

        gameWorld.build();
    }

    /**
     * initialize connections with the player, accepting a new jet and sending all entities to the client.
     * @param receive  the incoming communication from the player
     * @param send the outgoing communication to the player
     * @param asAdmin if true, the connection has admin capabilities
     * @throws IOException if the connection could not be established
     */
    public void connectToPlayer(InputStream receive, OutputStream send, boolean asAdmin) throws IOException {
        // establish communication handler
        ServerConnection player = new ServerConnection(
                receive, send,
                this, (spawn, id) -> connections.forEach(conn -> conn.sendEntitySpawn(spawn)),
                gameWorld.getNewSpawnPosition(), gameWorld.getCurrentType(), gameWorld,
                asAdmin
        );

        // send all entities until this point (excluding the player jet himself)
        for (MovingEntity entity : gameWorld.getEntities()) {
            player.sendEntitySpawn(entity.getFactory());
        }

        AbstractJet entity = player.jet();
        gameWorld.addEntity(entity);
        connections.forEach(conn -> conn.sendPlayerSpawn(player));
        gameWorld.updateGameLoop(globalTime.getGameTime().current(), globalTime.getGameTime().difference());

        connections.add(player);
        connections.forEach(ServerConnection::flush);
        new Thread(player::listen).start();
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
        connections.stream()
                .filter(conn -> conn.jet() == jet).findFirst()
                .ifPresent(conn -> conn.sendPowerupCollect(newType));
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
        if (worldShouldSwitch) setWorld(raceWorld);
        connections.removeIf(ServerConnection::isClosed);

        globalTime.updateGameTime();
        TrackedFloat time = globalTime.getGameTime();
        gameWorld.updateGameLoop(time.current(), time.difference());

        Collection<MovingEntity> entities = gameWorld.getEntities();

        for (MovingEntity ety : entities) {
            if (ety instanceof PowerupEntity) continue;

            if (TemporalEntity.isOverdue(ety)) {
                connections.forEach(conn -> conn.sendEntityRemove(ety));
                gameWorld.removeEntity(ety);

            } else {
                connections.forEach(conn -> conn.sendEntityUpdate(ety, time.current()));
            }
        }
        connections.forEach(ServerConnection::flush);
    }

    @Override
    public void unPause() {
        globalTime.unPause();
        super.unPause();
    }

    @Override
    public void startRace() {
        worldShouldSwitch = true;
    }

    @Override
    public void pause() {
        globalTime.pause();
        super.pause();
    }

    @Override
    public void shutDown() {
        stopLoop();
    }

    @Override
    public void stopLoop() {
        connections.forEach(ServerConnection::sendShutDown);
        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        gameWorld.cleanUp();
    }

    private void setWorld(EnvironmentClass world) {
        Logger.INFO.print("Switching world to " + world);
        connections.forEach(conn -> conn.sendWorldSwitch(world));
        // startup new world
        Player[] asArray = connections.toArray(new Player[0]);
        RaceProgress raceProgress = new RaceProgress(asArray.length, this, asArray);
        gameWorld.setContext(this, raceProgress);
        gameWorld.switchTo(world);

        for (ServerConnection player : connections) {
            AbstractJet jet = player.jet();
            jet.set(gameWorld.getNewSpawnPosition());
            player.sendEntityUpdate(jet, globalTime.time());
            jet.setPowerup(PowerupType.NONE);
            player.sendPowerupCollect(PowerupType.NONE);

            gameWorld.addEntity(jet);
            EntityFactory jetFactory = jet.getFactory();
            connections.stream().filter(o -> o != player).forEach(conn -> conn.sendEntitySpawn(jetFactory));
        }

        worldShouldSwitch = false;

        // add FUN
        for (int i = 0; i < ServerSettings.NOF_FUN; i++)
            new Thread(() -> {
            MovingEntity target = connections.get(0).jet();
                EntityFactory blueprint = new JetSpitsy.Factory(gameWorld.getNewSpawnPosition(), 0);

            AbstractJet npc = (AbstractJet) blueprint.construct(this, gameWorld);
                Logger.printOnline(npc::getPlaneDataString);

            Controller controller = new HunterAI(npc, target, gameWorld, JetBasic.THROTTLE_POWER / JetBasic.AIR_RESISTANCE_COEFFICIENT);
            npc.setController(controller);
            gameWorld.addEntity(npc);
            connections.forEach(conn -> conn.sendEntitySpawn(blueprint));
        }).start();
    }

    public void playerCheckpointUpdate(Player p, int checkpointProgress, int roundProgress) {
        connections.forEach(conn -> conn.sendProgress(p.playerName(), checkpointProgress, roundProgress));
    }
}
