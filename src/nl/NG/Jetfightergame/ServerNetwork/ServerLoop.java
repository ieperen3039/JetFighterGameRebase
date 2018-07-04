package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.RaceProgress.RaceChangeListener;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static nl.NG.Jetfightergame.Settings.ServerSettings.COLLISION_DETECTION_LEVEL;

/**
 * mostly a controlling layer between server and an environment
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ServerLoop extends AbstractGameLoop implements GameServer, RaceChangeListener {

    private final List<ServerConnection> connections;

    private GameState gameWorld;
    private int playersInLobby = 0;
    private GameTimer globalTime;
    private List<Player> players;

    public ServerLoop(GameState world) {
        super("Server", ServerSettings.TARGET_TPS, true);
        this.gameWorld = world;
        this.globalTime = new GameTimer();
        this.connections = new ArrayList<>();
        this.players = new ArrayList<>();

        gameWorld.buildScene(this, new RaceProgress(), COLLISION_DETECTION_LEVEL, true);
    }

    /**
     * initialize connections with the player, accepting a new jet and sending all entities to the client.
     * @param socket the socket connection of this player, accepted and without listeners on its streams
     * @param asAdmin if true, the connection has admin capabilities
     * @throws IOException if the connection could not be established
     */
    public void connectToPlayer(Socket socket, boolean asAdmin) throws IOException {
        // establish communication handler
        ServerConnection connector = new ServerConnection(socket, asAdmin, this, gameWorld.getNewSpawn());
        // first thing to do is creating a player
        MovingEntity player = connector.jet();
        connections.add(connector);
        new Thread(connector::listen).start();

        // send all entities until this point (excluding the player himself)
        for (MovingEntity entity : gameWorld.getEntities()) {
            EntityClass type = EntityClass.get(entity);
            Spawn spawn = new Spawn(type, entity.getPosition(), entity.getRotation(), entity.getVelocity());
            connector.sendEntitySpawn(spawn, entity.idNumber());
            connector.sendPlayerSpawn(connector);
        }

        playersInLobby++;
        players.add(connector);
        gameWorld.addEntity(player);
//        lobby.updateGameLoop(globalTime.getGameTime().current(), globalTime.getGameTime().difference());
    }

    @Override
    public void addSpawn(Spawn spawn){
        MovingEntity entity = spawn.construct(this, Controller.EMPTY);
        gameWorld.addEntity(entity);
        connections.forEach(c -> c.sendEntitySpawn(spawn, entity.idNumber()));
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
    protected void update(float deltaTime) {
        // deltaTime is real-time difference
        if (playersInLobby != 0) update(gameWorld);
        if (gameWorld != null) update(gameWorld);

        connections.forEach(ServerConnection::flush);
    }

    private void update(GameState world) {
        globalTime.updateGameTime();
        world.updateGameLoop(globalTime.getGameTime().current(), globalTime.getGameTime().difference());

        float time = globalTime.time();
        Collection<MovingEntity> entities = world.getEntities();

        for (MovingEntity object : entities) {
            if (TemporalEntity.isOverdue(object)) {
                connections.forEach(conn -> conn.sendEntityRemove(object));
                world.removeEntity(object);

            } else {
                connections.forEach(conn -> conn.sendEntityUpdate(object, time));
            }
        }
    }

    @Override
    public void unPause() {
        globalTime.unPause();
        super.unPause();
    }

    @Override
    public void pause() {
        globalTime.pause();
        super.pause();
    }

    @Override
    public void shutDown() {
        connections.forEach(ServerConnection::close);
        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        gameWorld.cleanUp();
    }

    @Override
    protected void exceptionHandler(Exception ex) {
        connections.forEach(ServerConnection::close);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(super.toString());
        gameWorld.getEntities().forEach(e -> s.append("\n").append(e));
        return s.toString();
    }

    public void startMap(WorldClass world) {
        // breakdown
        connections.forEach(conn -> conn.sendWorldSwitch(world));
        gameWorld = world.get();

        Player[] asArray = (Player[]) players.toArray();
        RaceProgress raceProgress = new RaceProgress(playersInLobby, this, asArray);
        gameWorld.buildScene(this, raceProgress, COLLISION_DETECTION_LEVEL, false);
    }

    public void playerCheckpointUpdate(Player p, int n, int r) {
        connections.forEach(conn -> conn.sendProgress(p.playerName(), n, r));
    }
}
