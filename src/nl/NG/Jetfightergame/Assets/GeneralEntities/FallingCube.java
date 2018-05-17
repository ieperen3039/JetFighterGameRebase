package nl.NG.Jetfightergame.Assets.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 26-12-2017.
 */
public class FallingCube extends GameEntity {

    private final float range;
    protected Material surfaceMaterial;

    /**
     * a cube that can be moved around, and has all physic properties
     * @param id the id of this gameEntity
     * @param renderTimer   the timer of the rendering, in order to let {@link MovingEntity#interpolatedPosition()}
     *                      return the interpolated position
     * @param entityDeposit new entities are passed here
     */
    public FallingCube(int id, GameTimer renderTimer, SpawnReceiver entityDeposit) {
        this(id, Material.ROUGH, 1, 1, PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf(), renderTimer, entityDeposit);
    }

    /**
     * a cube that can be moved around, and has all physic properties
     * @param id
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param renderTimer     the timer of the rendering, in order to let {@link MovingEntity#interpolatedPosition()}
*                        return the interpolated position
     * @param entityDeposit   new entities are passed here, when this entity seizes control of it
     */
    public FallingCube(int id, PosVector initialPosition,
                       DirVector initialVelocity, Quaternionf initialRotation, GameTimer renderTimer, SpawnReceiver entityDeposit) {
        this(id, Material.ROUGH, 1, 1, initialPosition, initialVelocity, initialRotation, renderTimer, entityDeposit);
    }

    /**
     * a cube that can be moved around, and has all physic properties
     * @param id
     * @param surfaceMaterial material properties
     * @param mass            in kg
     * @param scale           scalefactor applied to this object. the scale is in global space and executed in {@link
*                        #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param renderTimer     the timer of the rendering, in order to let {@link MovingEntity#interpolatedPosition()}
*                        return the interpolated position
     * @param entityDeposit   new entities are passed here, when this entity seizes control of it
     */
    public FallingCube(int id, Material surfaceMaterial, float mass, float scale, PosVector initialPosition,
                       DirVector initialVelocity, Quaternionf initialRotation, GameTimer renderTimer, SpawnReceiver entityDeposit) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, scale, renderTimer, entityDeposit);
        this.range = (float) Math.sqrt(3 * scale * scale);
        this.surfaceMaterial = surfaceMaterial;
    }

    /**
     * ad a random twitch to the object
     * @param factor arbitrary factor. higher is more rotation, 0 is no rotation
     */
    public void addRandomRotation(float factor) {
        yawSpeed += (ServerSettings.random.nextFloat() - 0.5f) * factor;
        pitchSpeed += (ServerSettings.random.nextFloat() - 0.5f) * factor;
        rollSpeed += (ServerSettings.random.nextFloat() - 0.5f) * factor;
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        velocity.add(netForce.scale(deltaTime / mass, extraVelocity), extraVelocity);
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
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

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(surfaceMaterial);
    }
}
