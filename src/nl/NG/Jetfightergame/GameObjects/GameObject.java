package nl.NG.Jetfightergame.GameObjects;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.GLMatrix.ShadowMatrix;
import nl.NG.Jetfightergame.GameObjects.Hitbox.Collision;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.FloatInterpolator;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.VectorInterpolator;
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

    private static final int INTERPOLATION_QUEUE_SIZE = 5;

    /** worldspace position in m */
    protected PosVector position;
    /** worldspace movement in m/s */
    protected DirVector velocity;
    /** absolute rotation in radians */
    protected float rotation;
    /** rotation speed in radian/s */
    protected float rotationSpeed;

    private float drawTimer = 0;
    private VectorInterpolator positionInterpolator;
    private FloatInterpolator rotationInterpolator;

    /** extrapolated worldspace position */
    protected PosVector extraPosition;
    /** expected rotation in radians */
    protected float extraRotation;

    /** rotation axis in worldspace */
    protected DirVector rotationAxis;

    /** collision of this gametick, null if it doesn't hit */
    protected Extreme<Collision> nextCrash;
    /** cached positions of the hitpoints*/
    private Collection<TrackedVector<PosVector>> hitPoints = null;

    /** worldspace / localspace */
    private float scale;
    private Material surfaceMaterial;
    protected final float mass;

    /**
     * any object that may be moved and hit other objects, is a game object
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param surfaceMaterial material properties
     * @param scale scalefactor applied to this object. the scale is in global space and executed in {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialRotation the initial rotation around the Z-axis of this object in radians
     */
    public GameObject(PosVector initialPosition, Material surfaceMaterial, float scale, float initialRotation, float mass) {
        this.position = initialPosition;
        this.surfaceMaterial = surfaceMaterial;
        rotationAxis = DirVector.Z;
        rotation = initialRotation;
        this.scale = scale;
        extraPosition = initialPosition;
        extraRotation = initialRotation;
        velocity = DirVector.O;
        this.mass = mass;

        positionInterpolator = new VectorInterpolator(INTERPOLATION_QUEUE_SIZE, position);
        rotationInterpolator = new FloatInterpolator(INTERPOLATION_QUEUE_SIZE, rotation);
    }

    @Override
    public void preUpdate(float deltaTime) {
        nextCrash = new Extreme<>(false);

        // 1st law of Newton
        DirVector netForce = DirVector.O;//TODO external influences
        applyPhysics(deltaTime, netForce);

        updateShape(deltaTime);
    }


    /**
     * update the shape of the airplane, if applicable
     */
    protected abstract void updateShape(float deltaTime);

    /**
     * @param relative a vector relative to this object
     * @param sm a frame of reference, the resulting vector will be in the space of the instantiation of this matrix
     * @return a vector in {@code sm}'s frame of reference
     */
    public DirVector relativeToWorldSpace(DirVector relative, MatrixStack sm) {
        final DirVector[] axis = new DirVector[1];

        toLocalSpace(sm, () -> axis[0] = sm.getDirection(relative), extraPosition, extraRotation);
        return axis[0];
    }

    public abstract void applyPhysics(float deltaTime, DirVector netForce);

    /**
     * update the state of this object, may not be called by any method from another interface
     * @param currentTime seconds between some startTime t0 and the begin of the current gameloop
     * @param deltaTime time since last update call
     */
    public void update(float currentTime, float deltaTime) {
        hitPoints = null;

        velocity = position.to(extraPosition).scale(deltaTime);
        rotationSpeed = (extraRotation - rotation) * deltaTime;
        position = extraPosition;
        rotation = extraRotation;

        positionInterpolator.add(position, currentTime);
        rotationInterpolator.add(rotation, currentTime);
    }

    @Override
    public boolean checkCollisionWith(Touchable other){
        Collision newCollision = getHitpointMovement().stream()
                // see which points collide with the world
                .map(point -> getPointCollision(point, other))
                // exclude points that didn't hit
                .filter(Objects::nonNull)
                // select first point hit
                .min(Collision::compareTo)
                // return the final rotation
                .orElse(null);

        // if there is a collision earlier than the currently registered collision
        nextCrash.check(newCollision);

        return newCollision != null;
    }

    /**
     * returns the collisions caused by {@code point} in the given reference frame.
     * the returned collision is caused by the first plane hit by point
     * @param point a point in global space
     * @param other another object
     * @return the first collision caused by this point
     */
    private Collision getPointCollision(TrackedVector<PosVector> point, Touchable other) {

        Stream.Builder<Collision> multipliers = Stream.builder();

        // collect the collisions
        final ShadowMatrix identity = new ShadowMatrix();
        final Consumer<Shape> exec = shape -> {
            // map points to local space
            PosVector startPoint = identity.getReversePosition(point.previous());
            PosVector endPoint = identity.getReversePosition(point.current());
            DirVector direction = startPoint.to(endPoint);
            // search hitpoint, add it when found
            Collision stopVec = shape.getMaximumMovement(startPoint, direction, endPoint);
            if (stopVec != null) multipliers.add(stopVec);
        };

        other.toLocalSpace(identity, () -> create(identity , exec, true));

        // iterate over all collisions
        return multipliers.build()
                // select the smallest
                .min(Collision::compareTo)
                .orElse(null);
    }

    /**
     * collects the movement of the hitpoints of this object for the current state, and caches it
     * @return a collection of the positions of the hitpoints in world space
     * TODO optimization
     */
    private Collection<TrackedVector<PosVector>> getHitpointMovement() {
        if (hitPoints == null) {

            final List<PosVector> previous = getCurrentPosition();
            final List<PosVector> current = getNextPosition();

            // combine both lists into one list
            List<TrackedVector<PosVector>> points = new ArrayList<>(previous.size());
            Iterator<PosVector> previousPoints = previous.iterator();
            Iterator<PosVector> currentPoints = current.iterator();
            while (previousPoints.hasNext()) {
                points.add(new TrackedVector<>(previousPoints.next(), currentPoints.next()));
            }
            hitPoints = points;
        }

        return hitPoints;
    }

    // collect the extrapolated position of the points of this object in worldspace
    private List<PosVector> getNextPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> current = new ArrayList<>();
        final Consumer<Shape> collectCurrent = (shape -> shape.getPoints().stream()
                .map(identity::getPosition)
                .forEach(current::add)
        );
        toLocalSpace(identity, () -> create(identity, collectCurrent, true), true);
        return current;
    }

    // collect the current position of the points of this object in worldspace
    private List<PosVector> getCurrentPosition() {
        ShadowMatrix identity = new ShadowMatrix();
        final List<PosVector> previous = new ArrayList<>();
        final Consumer<Shape> collectPrevious = (shape -> shape.getPoints().stream()
                .map(identity::getPosition)
                .forEach(previous::add)
        );
        toLocalSpace(identity, (() -> create(identity, collectPrevious, false)), false);
        return previous;
    }

    public void toLocalSpace(MatrixStack ms, Runnable action, boolean extrapolate) {
        PosVector currPos = extrapolate ? extraPosition : position;
        float currRot = extrapolate ? extraRotation : rotation;

        toLocalSpace(ms, action, currPos, currRot);
    }

    private void toLocalSpace(MatrixStack ms, Runnable action, PosVector currPos, float currRot) {
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

    @Override
    public void draw(GL2 gl, float currentTime) {
        PosVector pos = getPosition(currentTime);
        Float rot = rotationInterpolator.getInterpolated(currentTime);

        preDraw(gl);
        Consumer<Shape> painter = gl::draw;
        toLocalSpace(gl, () -> create(gl, painter), pos, rot);
    }

    /** returns the latest calculated position, but one should preferably use {@link #getPosition(float)}*/
    public PosVector getPosition() {
        return position;
    }

    @Override
    public PosVector getPosition(float currentTime) {
        if (currentTime < drawTimer) throw new IllegalArgumentException(
                "currentTime must be larger than any earlier call of currentTime\n" +
                        "earlier call was " + drawTimer + ", this call was " + currentTime
        );
        drawTimer = currentTime;

        return positionInterpolator.getInterpolated(currentTime).toPosVector();
    }

    @Override
    public DirVector getVelocity() {
        return velocity;
    }

    /** an object that represents {@code null}. Making this appear in-game is an achievement */
    public static final GameObject EMPTY_OBJECT = new GameObject(PosVector.O, Material.GLOWING, 1f , 0f, 1f) {
        public void create(MatrixStack ms, Consumer<Shape> action, boolean b) {}
        public void draw(GL2 ms) {Toolbox.drawAxisFrame(ms);}
        public void applyCollision() {}
        protected void updateShape(float deltaTime) {}
        public void applyPhysics(float deltaTime, DirVector netForce) {position.add(netForce.scale(deltaTime));}
    };
}
