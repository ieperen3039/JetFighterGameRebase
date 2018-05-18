package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity.Spawn;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Settings.ServerSettings;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * mostly a controlling layer between server and an environment
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ServerLoop extends AbstractGameLoop implements GameServer {

    private final Environment world;
    private final Collection<ServerConnection> connections;

    public ServerLoop(Environment world, Consumer<Exception> exceptionHandler) {
        super("Server", ServerSettings.TARGET_TPS, true, exceptionHandler);
        this.world = world;
        connections = new ArrayList<>();
    }

    /**
     * initialize connections with the player, accepting a new jet and sending all entities to the client.
     * @param socket the socket connection of this player, accepted and without listeners on its streams
     * @param asAdmin if true, the connection has admin capabilities
     * @throws IOException if the connection could not be established
     */
    public void connectToPlayer(Socket socket, boolean asAdmin) throws IOException {
        // establish communication handler
        ServerConnection connector = new ServerConnection(socket, asAdmin, this, world.getNewSpawn());
        // first thing to do is creating a player
        MovingEntity player = connector.getPlayerJet();
        connections.add(connector);
        new Thread(connector::listen).start();

        // send all entities until this point (excluding the player itself)
        for (MovingEntity entity : world.getEntities()) {
            EntityClass type = EntityClass.get(entity);
            Spawn spawn = new Spawn(type, entity.getPosition(), entity.getRotation(), entity.getVelocity());
            connector.sendEntitySpawn(spawn, entity.idNumber());
        }

        world.addEntity(player);
        world.updateGameLoop();
    }

    @Override
    public void addSpawn(Spawn spawn){
        MovingEntity entity = spawn.construct(this, Controller.EMPTY);
        world.addEntity(entity);
        connections.forEach(c -> c.sendEntitySpawn(spawn, entity.idNumber()));
    }

    @Override
    public void addParticles(ParticleCloud newParticles) {
        // no
    }

    @Override
    public GameTimer getTimer() {
        return world.getTimer();
    }

    @Override
    protected void update(float deltaTime) {
        world.getTimer().updateGameTime();
        world.updateGameLoop();

        float time = world.getTimer().time();
        Collection<MovingEntity> entities = world.getEntities();

        connections.parallelStream()
                .forEach(conn -> entities.forEach(e -> conn.sendEntityUpdate(e, time)));
        connections.forEach(ServerConnection::flush);
    }

    @Override
    public void unPause() {
        world.getTimer().unPause();
        super.unPause();
    }

    @Override
    public void pause() {
        world.getTimer().pause();
        super.pause();
    }

    @Override
    public void shutDown() {
        connections.forEach(ServerConnection::close);
        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        world.cleanUp();
    }

    public String entityList() {
        StringBuilder s = new StringBuilder();
        world.getEntities().forEach(e -> s.append("\n").append(e));
        return s.toString();
    }
}
