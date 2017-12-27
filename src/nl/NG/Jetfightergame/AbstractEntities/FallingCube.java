package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.ShapeCreators.ShapeDefinitions.GeneralShapes;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 26-12-2017.
 */
public class FallingCube extends GameEntity {
    /**
     * a cube that can be moved around, and has all physic properties
     *
     * @param surfaceMaterial material properties
     * @param mass            in kg
     * @param scale           scalefactor applied to this object. the scale is in global space and executed in
     *                        {@link #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param renderTimer     the timer of the rendering, in order to let {@link MovingEntity#interpolatedPosition()}
     *                        return the interpolated position
     */
    public FallingCube(Material surfaceMaterial, float mass, float scale, PosVector initialPosition, DirVector initialVelocity, Quaternionf initialRotation, TrackedFloat renderTimer) {
        super(surfaceMaterial, mass, scale, initialPosition, initialVelocity, initialRotation, renderTimer);
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public void applyPhysics(float deltaTime, DirVector netForce) {
        DirVector extraVelocity = new DirVector();
        netForce.scale(deltaTime/mass, extraVelocity).add(velocity, extraVelocity);
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public String toString() {
        return "Fallingcube {pos = " + position + ", dir = " + velocity +"}";
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean extrapolate) {
        action.accept(GeneralShapes.CUBE);
    }

    @Override
    public void applyCollision() {

    }
}
