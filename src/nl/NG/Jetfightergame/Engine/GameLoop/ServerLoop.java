package nl.NG.Jetfightergame.Engine.GameLoop;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity.SpawnEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.ServerNetwork.ServerConnection;
import nl.NG.Jetfightergame.Settings.ServerSettings;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
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
        ServerConnection connector = new ServerConnection(socket, asAdmin, world, this);
        // first thing to do is creating a player
        MovingEntity player = connector.getPlayer(world.getNewSpawn());
        connections.add(connector);
        new Thread(connector::listen).start();

        // send all entities until this point (excluding the player itself)
        for (MovingEntity entity : world.getEntities()) {
            EntityClass type = EntityClass.get(entity);
            SpawnEntity spawn = new SpawnEntity(type, entity.getPosition(), entity.getRotation(), entity.getVelocity());
            connector.sendEntitySpawn(spawn, entity.idNumber());
        }

        world.addEntity(player);
    }

    @Override
    public void spawnEntity(SpawnEntity spawn, Controller playerInput){
        MovingEntity entity = spawn.construct(world, playerInput);
        world.addEntity(entity);
        connections.forEach(c -> c.sendEntitySpawn(spawn, entity.idNumber()));
    }

    @Override
    protected void update(float deltaTime) {
        world.getTimer().updateGameTime();
        world.updateGameLoop();

        float time = world.getTimer().time();
        Collection<MovingEntity> entities = world.getEntities();

        connections.parallelStream()
                .forEach(conn -> entities.forEach(e -> conn.sendEntityUpdate(e, time)));
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
        super.stopLoop();
    }

    @Override
    protected void cleanup() {
        world.cleanUp();
    }
}
