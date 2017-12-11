package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Controllers.PlayerController;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.FighterJets.TestJet;
import nl.NG.Jetfightergame.GameObjects.AbstractJet;
import nl.NG.Jetfightergame.GameObjects.GameObject;
import nl.NG.Jetfightergame.GameObjects.MovingObject;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Primitives.Particles.AbstractParticle;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Geert van Ieperen
 * created on 11-12-2017.
 */
public class GameState {

    private final Controller playerInput = new PlayerController();
    private AbstractJet playerJet = new TestJet(playerInput);

    protected Collection<GameObject> objects = new LinkedList<>();
    protected Collection<Touchable> staticObjects = new LinkedList<>();
    protected Collection<AbstractParticle> particles = new LinkedList<>();
    protected Collection<Pair<PosVector, Color4f>> lights = new LinkedList<>();

    private int debugVariable = 0;

    /**
     * update the physics of all game objects and check for collisions
     * @param deltaTime time since last renderloop
     */
    public void updateGameLoop(float deltaTime) {
        // update positions with respect to collisions
        objects.forEach((gameObject) -> gameObject.preUpdate(deltaTime));
        if (Settings.UNIT_COLLISION) {
            getIntersectingPairs().parallelStream()
                    .forEach(GameState::checkPair);
        }
        objects.forEach(MovingObject::postUpdate);
    }

    /**
     * let each object of the pair check for collisions, but does not make any changes just yet.
     * these only take effect after calling {@link MovingObject#postUpdate()}
     * @param p a pair of objects that may have collided.
     */
    private static void checkPair(Pair<Touchable, MovingObject> p) {
        Touchable either = p.left;
        MovingObject moving = p.right;

        moving.checkCollisionWith(either);
        if (either instanceof MovingObject) ((MovingObject) either).checkCollisionWith(moving);
    }

    protected void buildScene() {
//        objects.add(new TestJet(gameLoop, playerInput));
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    /** TODO efficient implementation
     * generate a list (possibly empty) of all objects that may have collided.
     * this may include (parts of) the ground, but not an object with itself.
     * one pair should not occur the other way around
     *
     * @return a collection of pairs of objects that are close to each other
     */
    private Collection<Pair<Touchable, MovingObject>> getIntersectingPairs() {
        final Collection<Pair<Touchable, MovingObject>> result = new LinkedList<>();

        // Naive solution: return all n^2 options
        // check all moving objects against (1: all other moving objects, 2: all static objects)
        objects.parallelStream().forEach(obj -> {
            objects.stream()
                    .filter(o -> obj != o) // only other objects
                    .forEach(other -> result.add(new Pair<>(other, obj)));
            staticObjects
                    .forEach(other -> result.add(new Pair<>(other, obj)));
        });

        Toolbox.printSpamless("created " + result.size() + " combinations");
        return result;
    }

    public void setLights(GL2 gl) {
        lights.forEach((pointLight) -> gl.setLight(pointLight.left, pointLight.right));
    }

    /**
     * draw all objects of the game
     * @param gl
     */
    public void drawObjects(GL2 gl) {
        gl.pushMatrix();
        gl.rotate(DirVector.Z, (debugVariable++) / 40f);
        Toolbox.drawAxisFrame(gl);
        gl.popMatrix();
        staticObjects.forEach(d -> d.draw(gl));
        objects.forEach(d -> d.draw(gl));
    }

    public void drawParticles(GL2 gl){
        particles.forEach(gl::draw);
    }

    /**
     * update the position of the particles.
     * should be called every renderloop
     * @param elapsedSeconds time since last renderframe
     */
    public void updateParticles(float elapsedSeconds) {
        particles.forEach(p -> p.updateRender(elapsedSeconds));
    }

    public AbstractJet getPlayer() {
        return playerJet;
    }
}
