package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.Engine.GameLoop.AbstractGameLoop;
import nl.NG.Jetfightergame.GameObjects.Hitbox.Collision;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *         created on 29-10-2017.
 */
public abstract class GameObject implements MovingObject {

    protected final AbstractGameLoop engine;

    /** worldspace position */
    protected TrackedVector<PosVector> position;
    /** extrapolated worldspace position */
    protected PosVector extraPosition;
    /** worldspace movement in m/s */
    protected DirVector movement;
    /** netForce applied on this object in worldspace */
    protected DirVector netForce;
    /** rotation axis in worldspace */
    protected DirVector rotationAxis;
    /** absolute rotation in radians */
    protected TrackedFloat rotation;
    /** expected rotation in radians */
    protected float extraRotation;
    /** rotationspeed in rad/s */
    protected float rotationSpeed;
    /** collision of this gametick, null if it doesn't hit */
    protected Extreme<Collision> nextCrash;
    /** cached positions of the hitpoints*/
    private Collection<TrackedVector<PosVector>> hitPoints = null;
    /** worldspace / localspace */
    private float scale;
    private Material surfaceMaterial;

    /**
     * any object that may be moved and hit other objects, is a game object
     * @param engine reference to the game engine
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param surfaceMaterial material properties
     * @param scale scalefactor applied to this object. the scale is in global space and executed in {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialRotation the initial rotation around the Z-axis of this object in radians
     */
    public GameObject(AbstractGameLoop engine, PosVector initialPosition, Material surfaceMaterial, float scale, float initialRotation) {
        this.engine = engine;
        this.position = new TrackedVector<>(initialPosition);
        this.surfaceMaterial = surfaceMaterial;
        movement = DirVector.O;
        rotationAxis = DirVector.Z;
        rotation = new TrackedFloat(initialRotation);
        rotationSpeed = 0;
        this.scale = scale;
        extraPosition = initialPosition;
        extraRotation = initialRotation;
        netForce = DirVector.O;
    }

    @Override
    public void preUpdate(float deltaTime) {
        applyPhisics(deltaTime);
        // 1st law of Newton
        nextCrash = new Extreme<>(false);
        // collect extrapolated variables
        extraPosition = position.current().add(movement);
        extraRotation = rotation.current() + (rotationSpeed * deltaTime);
    }

    public DirVector getRelativeVector(DirVector xVec, MatrixStack sm) {
        final DirVector[] axis = new DirVector[1];
        toLocalSpace(sm, () -> axis[0] = sm.getDirection(xVec), false);
        return axis[0];
    }

    public abstract void applyPhisics(float deltaTime);

    @Override
    public void postUpdate() {
        hitPoints = null;
    }

    /**
     * collects the movement of the hitpoints of this object for the current state, and caches it
     * @return a collection of the positions of the hitpoints in world space
     */
    private Collection<TrackedVector<PosVector>> getHitpointMovement() {
        if (hitPoints == null) {

            final ShadowMatrix identity = new ShadowMatrix();

            // collect the previous position of the points of this object in worldspace
            final List<PosVector> previous = new LinkedList<>();
            final Consumer<Shape> collectPrevious = (shape -> shape.getPoints().stream()
                    .map(identity::getPosition)
                    .forEach(previous::add)
            );
            toLocalSpace(identity, (() -> create(identity, collectPrevious, true)), true);

            // collect the current position of the points of this object in worldspace
            final List<PosVector> current = new LinkedList<>();
            final Consumer<Shape> collectCurrent = (shape -> shape.getPoints().stream()
                    .map(identity::getPosition)
                    .forEach(current::add)
            );
            toLocalSpace(identity, (() -> create(identity, collectCurrent, false)), false);

            // combine both lists into one list
            List<TrackedVector<PosVector>> points = new LinkedList<>();
            Iterator<PosVector> previousPoints = previous.iterator();
            Iterator<PosVector> currentPoints = current.iterator();
            while (previousPoints.hasNext()) {
                points.add(new TrackedVector<>(previousPoints.next(), currentPoints.next()));
            }
            hitPoints = points;
        }

        return hitPoints;
    }

    @Override
    public void checkCollisionWith(Touchable other){
        Collision newCollision = getHitpointMovement().stream()
                // see which points collide with the world
                .map(point -> getCollisionEffect(point, other))
                // exclude points that didn't hit
                .filter(Objects::nonNull)
                // select first point hit
                .min(Collision::compareTo)
                // return the final rotation
                .orElse(null);

        nextCrash.check(newCollision);
    }

    /**
     * returns the rotations caused by {@code point} in the given reference frame.
     * this rotation is caused by the first plane hit by point
     * @param point a point in global space
     * @param other another object
     * @return the rotation caused by this point
     */
    private Collision getCollisionEffect(TrackedVector<PosVector> point, Touchable other) {

        Stream.Builder<Collision> multipliers = Stream.builder();

        // collect the collisions
        final ShadowMatrix identity = new ShadowMatrix();
        final Consumer<Shape> exec = (shape -> {
            // map points to local space
            PosVector startPoint = identity.getReversePosition(point.previous());
            PosVector endPoint = identity.getReversePosition(point.current());
            DirVector direction = startPoint.to(endPoint);
            // search hitpoint, add it when found
            Collision stopVec = shape.getMaximumMovement(startPoint, direction, endPoint);
            if (stopVec != null) multipliers.add(stopVec);
        });
        other.toLocalSpace(identity, () -> create(identity , exec, false), false);

        // iterate over all collisions
        return multipliers.build()
                // select the smallest
                .min(Collision::compareTo)
                .orElse(null);
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action, boolean takeStable){
        PosVector currPos = takeStable ? position.current() : extraPosition;
        double currRot = takeStable ? rotation.current() : extraRotation;

        ms.pushMatrix();
        {
            ms.translate(currPos);
            ms.rotate(rotationAxis, currRot);
            ms.scale(scale);
            action.run();
        }
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }

    public void addForce(DirVector force) {
        netForce = netForce.add(force);
    }

    @Override
    public PosVector getPosition() {
        return position.current();
    }

    @Override
    public DirVector getMovement() {
        return movement;
    }
}
