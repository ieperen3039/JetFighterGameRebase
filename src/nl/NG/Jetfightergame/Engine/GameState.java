package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.Scenarios.Environment;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.NG.Jetfightergame.Engine.Settings.DEBUG;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public abstract class GameState implements Environment {

    private AbstractJet playerJet;

    protected Collection<Touchable> staticEntities = new ArrayList<>();
    protected Collection<MovingEntity> dynamicEntities = new ArrayList<>();
    protected Collection<AbstractParticle> particles = new ArrayList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new ArrayList<>();

    private final GameTimer time = new GameTimer();

    private Collection<Pair<Touchable, MovingEntity>> allEntityPairs = null;
    private int totalCollisions;
    private final Consumer<ScreenOverlay.Painter> collisionCounter = (hud) -> hud.printRoll("Collision count: " + totalCollisions);

    public GameState(Controller input) {
        playerJet = new PlayerJet(input, time.getRenderTime());
        /* TODO playerjet responsibillity
         * Currently, the player is part of the world. It would be interesting if the playerJet is preserved along all
         * different worlds and stages of the game, even stored in savefiles. If not for structure, then just for the
         * idea that fits in the spirit of the game
         */
        ScreenOverlay.addHudItem(collisionCounter);
    }

    private Extreme<Integer> collisionMax = new Extreme<>(true);

    public abstract void buildScene();

    @Override
    @SuppressWarnings("ConstantConditions")
    public void updateGameLoop() {
        float currentTime = time.getGameTime().current();
        float deltaTime = time.getGameTime().difference();

        // update positions and apply physics
        dynamicEntities.parallelStream()
                .forEach((entity) -> entity.preUpdate(deltaTime, entityNetforce(entity)));

        int remainingLoops = Settings.MAX_COLLISION_ITERATIONS;
        // check and handle collisions
        if ((deltaTime > 0f) && (remainingLoops != 0)) {

            int newCollisions = 0;
            final Collection<Pair<Touchable, MovingEntity>> closeTargets = getIntersectingPairs();
            List<Pair<Touchable, MovingEntity>> collisionPairs;
            List<RigidBody> postCollisions = new ArrayList<>();

            do {
                /* as a single collision may result in a previously not-intersecting pair to collide,
                 * we shouldn't re-use the getIntersectingPairs method nor reduce by non-collisions.
                 * We should add some form of caching for getIntersectingPairs, to make short-followed calls more efficient.
                 */
                collisionPairs = closeTargets.stream()
                        // check for collisions
                        .filter(GameState::checkPair)
                        .collect(Collectors.toList());

                newCollisions += collisionPairs.size();
                postCollisions.clear();

                // process the final collisions in pairs
                postCollisions = collisionPairs.stream()
                        .map(ep -> new Pair<>(
                                ep.left.getFinalCollision(deltaTime),
                                ep.right.getFinalCollision(deltaTime)
                        ))
                        .flatMap(rp -> {
                            RigidBody.process(rp.left, rp.right);
                            return Stream.of(rp.left, rp.right);
                        })
                        .distinct()
                        .collect(Collectors.toList());

                // apply the collisions to the objects
                postCollisions
                        .forEach(r -> r.apply(deltaTime, currentTime));

            } while (!collisionPairs.isEmpty() && (--remainingLoops > 0));

            totalCollisions = newCollisions;
            if (remainingLoops == 0) {
                Toolbox.print(collisionPairs.size() + " collisions not resolved after " + newCollisions + " calculations");
            }
        }

        dynamicEntities.forEach(obj -> obj.update(currentTime));
    }

    protected abstract DirVector entityNetforce(MovingEntity entity);

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingEntity#getFinalCollision(float)}
     * @param p a pair of objects that may have collided.
     * @return true if this pair collided:
     * if this method returns false, then these objects do not collide and are not changed as a result.
     * if this method returns true, then these objects do collide and have this collision stored.
     * The collision can be calculated by {@link MovingEntity#getFinalCollision(float)} and applied by {@link Updatable#update(float)}
     */
    private static boolean checkPair(Pair<Touchable, MovingEntity> p) {
        MovingEntity moving = p.right;
        Touchable either = p.left;

        boolean change = moving.checkCollisionWith(either);
        if (either instanceof MovingEntity) {
            return change || ((MovingEntity) either).checkCollisionWith(moving);
        }
        return change;
    }

    /** TODO efficient implementation, Possibly move to dedicated class
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingEntity>> getIntersectingPairs() {
        if (allEntityPairs == null) {
            allEntityPairs = new ArrayList<>();

            // Naive solution: return all n^2 options
            // check all moving objects against (1: all other moving objects, 2: all static objects)
            dynamicEntities.forEach(obj -> {
                dynamicEntities.stream()
                        // this makes sure that one object pair does not occur the other way around.
                        .filter(other -> other.hashCode() >= obj.hashCode())
                        // as hashcode does not guarantees an identifier, we can not assume equality.
                        .filter(other -> !other.equals(obj))
                        .map(other -> new Pair<Touchable, MovingEntity>(other, obj))
                        .distinct()
                        .forEach(allEntityPairs::add);
                staticEntities
                        .forEach(other -> allEntityPairs.add(new Pair<>(other, obj)));
            });
            if (DEBUG) {
                final long nulls = allEntityPairs.stream().filter(Objects::isNull).count();
                if (nulls > 0) Toolbox.print("nulls found by intersecting pairs: " + nulls);
            }
        }


        collisionMax.updateAndPrint("Intersections", allEntityPairs.size(), "pairs");
        return allEntityPairs;
    }

    @Override
    public void setLights(GL2 gl) {
        for (Pair<PosVector, Color4f> l : lights) {
            final PosVector pos = l.left;
            final Color4f color = l.right;

            gl.setLight(pos, color);

            if (Settings.SHOW_LIGHT_POSITIONS){
                gl.setMaterial(Material.GLOWING, color);
                gl.pushMatrix();
                {
                    gl.translate(pos);
                    gl.scale(0.1f);
                    gl.draw(GeneralShapes.INVERSE_CUBE);
                }
                gl.popMatrix();
            }
        }
    }

    @Override
    public void drawObjects(GL2 gl) {
//        Toolbox.drawAxisFrame(gl);

        staticEntities.forEach(d -> d.draw(gl));
        dynamicEntities.forEach(d -> d.draw(gl));
    }

    @Override
    public void drawParticles(GL2 gl){
        particles.forEach(gl::draw);
    }

    @Override
    public void updateParticles() {
        particles.forEach(p -> p.updateRender(time.getRenderTime().difference()));
    }

    @Override
    public AbstractJet getPlayer() {
        return playerJet;
    }

    @Override
    public GameTimer getTimer() {
        return time;
    }

    /**
     * (this method may be redundant in the future)
     */
    public void cleanUp() {
        ScreenOverlay.removeHudItem(collisionCounter);
        dynamicEntities.clear();
        staticEntities.clear();
        particles.clear();
        System.gc();
    }
}
