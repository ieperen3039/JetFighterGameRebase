package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.*;
import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.BasicJet;
import nl.NG.Jetfightergame.Assets.Weapons.MachineGun;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
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
import org.joml.Quaternionf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        this.gameWorld = new EnvironmentManager(lobby, this, new RaceProgress(), true);
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
                this, (spawn, id) -> connections.forEach(conn -> conn.sendEntitySpawn(spawn, id)),
                gameWorld.getNewSpawnPosition(), gameWorld.getCurrentType(),
                asAdmin
        );

        // send all entities until this point (excluding the player jet himself)
        for (MovingEntity entity : gameWorld.getEntities()) {
            String type = entity.getTypeName();
            Prentity prentity = new Prentity(type, entity.getPosition(), entity.getRotation(), entity.getVelocity());
            player.sendEntitySpawn(prentity, entity.idNumber());
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
    public void addSpawn(Prentity prentity) {
        MovingEntity entity = prentity.construct(this);
        gameWorld.addEntity(entity);
        connections.forEach(conn -> conn.sendEntitySpawn(prentity, entity.idNumber()));
    }

    @Override
    public GameTimer getTimer() {
        return globalTime;
    }

    @Override
    public void addExplosion(PosVector position, DirVector direction, Color4f color1, Color4f color2, float power, int density) {
        connections.forEach(conn -> conn.sendExplosionSpawn(position, direction, power, color1, color2));
    }

    @Override
    public void powerupCollect(PowerupEntity powerup, float collectionTime, PowerupColor newType) {
        connections.forEach(conn -> conn.sendPowerupUpdate(powerup, collectionTime, newType));
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
        Logger.DEBUG.print("Switching world to " + world);
        connections.forEach(conn -> conn.sendWorldSwitch(world));
        // startup new world
        Player[] asArray = connections.toArray(new Player[0]);
        RaceProgress raceProgress = new RaceProgress(asArray.length, this, asArray);
        gameWorld.setContext(this, raceProgress);
        gameWorld.switchTo(world);

        // progress this change to all clients
        for (ServerConnection player : connections) {
            AbstractJet playerJet = player.jet();
            MovingEntity.State spawn = gameWorld.getNewSpawnPosition();
            playerJet.set(spawn);
        }

        connections.forEach(conn -> gameWorld.addEntity(conn.jet()));

        worldShouldSwitch = false;

        new Thread(() -> {
            MovingEntity target = connections.get(0).jet();
            Prentity blueprint = new Prentity(BasicJet.TYPE, new PosVector(100, 0, 30), new Quaternionf(), DirVector.zeroVector());

            AbstractJet npc = (AbstractJet) blueprint.construct(this);
            Controller controller = new RocketAI(npc, target, MachineGun.BULLET_SPEED);
            npc.setController(controller);
            gameWorld.addEntity(npc);
            connections.forEach(conn -> conn.sendEntitySpawn(blueprint, npc.idNumber()));
        }).start();
    }

    public void playerCheckpointUpdate(Player p, int checkpointProgress, int roundProgress) {
        connections.forEach(conn -> conn.sendProgress(p.playerName(), checkpointProgress, roundProgress));
    }
}
