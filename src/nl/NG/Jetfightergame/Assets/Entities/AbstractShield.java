package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.AbstractEntities.*;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes.ICOSAHEDRON;

/**
 * @author Geert van Ieperen. Created on 12-8-2018.
 */
@SuppressWarnings("NoopMethodInAbstractClass")
public abstract class AbstractShield extends MovingEntity implements TemporalEntity {
    protected final AbstractJet jet;
    protected float timeToLive;
    private float scale;

    public AbstractShield(int id, AbstractJet jet, GameTimer time, float timeToLive, SpawnReceiver deposit) {
        super(id, jet.getPosition(), jet.getVelocity(), jet.getRotation(), 0, time, deposit);
        this.jet = jet;
        this.timeToLive = timeToLive;
        this.scale = jet.getRange() * 1.2f;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        ms.scale(scale);
        ms.rotate(1f * renderTime(), 0, 0, 1);
        action.accept(ICOSAHEDRON);
        ms.popMatrix();
    }

    @Override
    public void preUpdate(float deltaTime, DirVector netForce) {
        super.preUpdate(deltaTime, netForce);
        timeToLive -= deltaTime;
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        jet.toLocalSpace(ms, action);
    }

    @Override
    public float getRange() {
        return scale;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return jet.getExpectedMiddle();
    }

    @Override
    public boolean isOverdue() {
        return timeToLive <= 0;
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        return null;
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
    }

    @Override
    public void update(float currentTime) {
        position = jet.getPosition();
    }

    @Override
    public EntityState getState() {
        return jet.getState();
    }

    public static abstract class ShieldFactory extends EntityFactory {
        protected int jetID = -1;

        public ShieldFactory() {
        }

        public ShieldFactory(EntityClass type, AbstractShield s) {
            super(type, s);
        }


        public ShieldFactory(EntityClass type, AbstractJet jet) {
            super(type, jet.getState(), 0);
            this.jetID = jet.idNumber();
        }

        @Override
        protected void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            jetID = in.readInt();
        }

        @Override
        protected void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeInt(jetID);
            if (jetID == id) throw new IllegalStateException();
        }

        protected AbstractJet getJet(EntityMapping entities) {
            if (jetID == -1) throw new IllegalStateException();

            MovingEntity entity = entities.getEntity(jetID);
            if (entity == null) throw new NoSuchElementException();
            assert entity instanceof AbstractJet;

            return (AbstractJet) entity;
        }
    }
}
