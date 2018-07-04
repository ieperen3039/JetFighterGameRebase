package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.TemporalEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.EnvironmentManager;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.RaceProgress.RaceChangeListener;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
    private int playersInLobby = 0;
    private GameTimer globalTime;

    public ServerLoop(EnvironmentClass world) {
        super("Server", ServerSettings.TARGET_TPS, true);
        this.gameWorld = new EnvironmentManager(world, false, this, new RaceProgress());
        this.globalTime = new GameTimer();
        this.connections = new ArrayList<>();
    }

    /**
     * initialize connections with the player, accepting a new jet and sending all entities to the client.
     * @param socket the socket connection of this player, accepted and without listeners on its streams
     * @param asAdmin if true, the connection has admin capabilities
     * @throws IOException if the connection could not be established
     */
    public void connectToPlayer(Socket socket, boolean asAdmin) throws IOException {
        // establish communication handler
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        ServerConnection player = new ServerConnection(
                out, in,
                this, this::addSpawn,
                gameWorld.getNewSpawnPosition(), gameWorld.getCurrentType(),
                asAdmin
        );

        // send all entities until this point (excluding the player himself)
        for (MovingEntity entity : gameWorld.getEntities()) {
            EntityClass type = EntityClass.get(entity);
            Spawn spawn = new Spawn(type, entity.getPosition(), entity.getRotation(), entity.getVelocity());
            player.sendEntitySpawn(spawn, entity.idNumber());
        }

        playersInLobby++;
        AbstractJet entity = player.jet();
        gameWorld.addEntity(entity);
        connections.forEach(conn -> conn.sendPlayerSpawn(player));
        gameWorld.updateGameLoop(globalTime.getGameTime().current(), globalTime.getGameTime().difference());

        connections.add(player);
        new Thread(player::listen).start();
    }

    @Override
    public void addSpawn(Spawn spawn){
        MovingEntity entity = spawn.construct(this, Controller.EMPTY);
        gameWorld.addEntity(entity);
        addSpawn(spawn, entity.idNumber());
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

    private void update(Environment world) {
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

    public void startMap(EnvironmentClass world) {
        // breakdown
        connections.forEach(conn -> conn.sendWorldSwitch(world));
        // startup
        Player[] asArray = connections.toArray(new Player[0]);
        RaceProgress raceProgress = new RaceProgress(asArray.length, this, asArray);
        gameWorld.setContext(this, raceProgress);
        gameWorld.switchTo(world);
    }

    public void playerCheckpointUpdate(Player p, int n, int r) {
        connections.forEach(conn -> conn.sendProgress(p.playerName(), n, r));
    }

    private void addSpawn(Spawn spawn, Integer id) {
        connections.forEach(conn -> conn.sendEntitySpawn(spawn, id));
    }
}
