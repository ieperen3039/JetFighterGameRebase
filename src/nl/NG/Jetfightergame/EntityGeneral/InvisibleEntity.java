package nl.NG.Jetfightergame.EntityGeneral;

import nl.NG.Jetfightergame.Assets.Entities.FallingCube;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * main purpose is to distract rockets
 * @author Geert van Ieperen. Created on 23-7-2018.
 */
public class InvisibleEntity extends FallingCube implements TemporalEntity {
    private float timeRemaining;

    private InvisibleEntity(int id, PosVector position, DirVector velocity, GameTimer renderTimer, SpawnReceiver entityDeposit, float duration) {
        super(id, Material.GLOWING, 1f, 0f, position, velocity, new Quaternionf(), renderTimer, entityDeposit);
        this.timeRemaining = duration;
    }

    @Override
    protected void updateShape(float deltaTime) {
        timeRemaining -= deltaTime;
    }

    @Override
    public ParticleCloud explode() {
        return null;
    }

    @Override
    public boolean isOverdue() {
        return timeRemaining < 0;
    }

    @Override
    public void draw(GL2 gl) {
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        return null;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public EntityFactory getFactory() {
        return new InvisibleEntity.Factory(position, velocity, timeRemaining);
    }

    public static class Factory extends EntityFactory {
        private float duration;

        public Factory() {
        }

        public Factory(PosVector position, DirVector velocity, float duration) {
            super(EntityClass.INVISIBLE_ENTITY, position, new Quaternionf(), velocity);
            this.duration = duration;
        }

        @Override
        protected void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeFloat(duration);
        }

        @Override
        protected void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            duration = in.readFloat();
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new InvisibleEntity(id, position, velocity, game.getTimer(), game, duration);
        }
    }
}
