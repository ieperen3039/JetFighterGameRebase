package nl.NG.Jetfightergame.Assets.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameState.EntityManager;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 26-12-2017.
 */
public class FallingCube extends GameEntity {

    private final float range;

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
     * @param entityDeposit
     */
    public FallingCube(Material surfaceMaterial, float mass, float scale, PosVector initialPosition,
                       DirVector initialVelocity, Quaternionf initialRotation, GameTimer renderTimer, EntityManager entityDeposit) {
        super(surfaceMaterial, mass, scale, initialPosition, initialVelocity, initialRotation, renderTimer, entityDeposit);
        this.range = (float) Math.sqrt(3 * scale * scale);
    }

    /**
     * ad a random twitch to the object
     * @param factor arbitrary factor. higher is more rotation, 0 is no rotation
     */
    public void addRandomRotation(float factor){
        yawSpeed += (Settings.random.nextFloat() - 0.5f) * factor;
        pitchSpeed += (Settings.random.nextFloat() - 0.5f) * factor;
        rollSpeed += (Settings.random.nextFloat() - 0.5f) * factor;
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        velocity.add(netForce.scale(deltaTime/mass, extraVelocity), extraVelocity);
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public String toString() {
        return "FallingCube " + idNumber();
    }

    @Override
    public void impact(PosVector impact, float power) {

    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        action.accept(GeneralShapes.CUBE);
    }

    @Override
    public float getRange() {
        return range;
    }
}
