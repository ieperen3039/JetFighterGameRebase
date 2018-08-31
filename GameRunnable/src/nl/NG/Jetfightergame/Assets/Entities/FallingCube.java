package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 26-12-2017.
 */
public class FallingCube extends MovingEntity {
    private final float range;
    private Material surfaceMaterial;
    private final float scale;

    /**
     * test entity cube of size 2*2*2 and mass 10.
     */
    public FallingCube(int id, PosVector position) {
        this(id, Material.SILVER, 100f, 1f, position, DirVector.zeroVector(), new Quaternionf(), new GameTimer(), null);
    }

    /**
     * a cube that can be moved around, and has all physic properties
     * @param id              the id of this gameEntity
     * @param surfaceMaterial material properties
     * @param mass            in kg
     * @param scale           scalefactor applied to this object. the scale is in global space and executed in {@link
     *                        #toLocalSpace(MatrixStack, Runnable, boolean)}
     * @param initialPosition position of spawining (of the origin) in world coordinates
     * @param initialVelocity the initial speed of this object in world coordinates
     * @param initialRotation the initial rotation of this object
     * @param renderTimer     the timer of this game
     * @param entityDeposit   new entities are passed here, when this entity seizes control of it
     */
    protected FallingCube(int id, Material surfaceMaterial, float mass, float scale, PosVector initialPosition,
                          DirVector initialVelocity, Quaternionf initialRotation, GameTimer renderTimer, SpawnReceiver entityDeposit
    ) {
        super(id, initialPosition, initialVelocity, initialRotation, mass, renderTimer, entityDeposit);
        this.scale = scale;
        this.range = (float) Math.sqrt(3 * scale * scale);
        this.surfaceMaterial = surfaceMaterial;
    }

    /**
     * ad a random twitch to the object
     * @param factor arbitrary factor. higher is more rotation, 0 is no rotation
     */
    public void addRandomRotation(float factor) {
        yawSpeed += (Toolbox.random.nextFloat() - 0.5f) * factor;
        pitchSpeed += (Toolbox.random.nextFloat() - 0.5f) * factor;
        rollSpeed += (Toolbox.random.nextFloat() - 0.5f) * factor;
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public void applyPhysics(DirVector netForce) {
        float deltaTime = gameTimer.getGameTime().difference();
        velocity.add(netForce.scale(deltaTime / mass, extraVelocity), extraVelocity);
        position.add(extraVelocity.scale(deltaTime, new DirVector()), extraPosition);
        rotation.rotate(rollSpeed * deltaTime, pitchSpeed * deltaTime, yawSpeed * deltaTime, extraRotation);
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return new PosVector(extraPosition);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        ms.scale(scale);
        action.accept(GeneralShapes.CUBE);
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.ROUGH);
    }

    public static class Factory extends EntityFactory {
        private Material material;
        private float mass;
        private float size;

        public Factory() {
            super();
        }

        public Factory(FallingCube cube) {
            super(EntityClass.FALLING_CUBE, cube);
            material = cube.surfaceMaterial;
            mass = cube.mass;
            size = cube.scale;
        }

        public Factory(PosVector pos, Quaternionf rot, DirVector vel, Material material, float mass, float size) {
            super(EntityClass.FALLING_CUBE, pos, rot, vel);
            this.material = material;
            this.mass = mass;
            this.size = size;
        }

        @Override
        public void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeInt(material.ordinal());
            out.writeFloat(mass);
            out.writeFloat(size);
        }

        @Override
        public void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            material = Material.get(in.readInt());
            mass = in.readFloat();
            size = in.readFloat();
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new FallingCube(id, material, mass, size, position, velocity, rotation, game.getTimer(), game);
        }
    }
}
