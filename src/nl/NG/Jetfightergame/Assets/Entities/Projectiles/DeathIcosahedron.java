package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.*;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityFactory;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 12-8-2018.
 */
public class DeathIcosahedron extends AbstractProjectile {
    private static final float SCALE = 3f;
    private static final float SPARK_COOLDOWN = 0.01f;
    private static final float FIRE_SPEED = 10f;
    private static final float SEEKER_COOLDOWN = 1f;
    public static final float SEEKER_LAUNCH_SPEED = 50f;
    private final EntityMapping entities;
    private float sparkTimeRemain;
    private float seekerTimeRemain;

    private DeathIcosahedron(
            int id, PosVector position, Quaternionf rotation, DirVector velocity,
            SpawnReceiver particleDeposit, GameTimer gameTimer, MovingEntity sourceEntity, EntityMapping entities
    ) {
        super(
                id, position, rotation, velocity, 1f,
                0f, 60f, 0f, 0f, 0f, 0f,
                particleDeposit, gameTimer, sourceEntity
        );
        this.entities = entities;
        sparkTimeRemain = 0;
        seekerTimeRemain = SEEKER_COOLDOWN;
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(10);
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.GLOWING, new Color4f(0.8f, 0.3f, 0));

        sparkTimeRemain -= gameTimer.getRenderTime().difference();
        if (sparkTimeRemain >= 0) return;

        ParticleCloud cloud = new ParticleCloud();
        do {
            PosVector pos = positionInterpolator.getInterpolated(renderTime() - sparkTimeRemain).toPosVector();
            DirVector move = positionInterpolator.getDerivative();
            move.add(DirVector.randomOrb().reducedTo(10f, new DirVector()));

            cloud.addParticle(pos, move, 0, 1, Color4f.RED, 2f);

            sparkTimeRemain += SPARK_COOLDOWN;
        } while (sparkTimeRemain < 0);

        entityDeposit.addParticles(cloud);
    }

    @Override
    public ParticleCloud explode() {
        return Particles.explosion(position, DirVector.zeroVector(), Color4f.YELLOW, Color4f.RED, 50, 1000, 2, 6f);
    }

    @Override
    protected void updateShape(float deltaTime) {
        super.updateShape(deltaTime);

        seekerTimeRemain -= deltaTime;
        if (seekerTimeRemain >= 0) return;

        do {
            float timeFraction = seekerTimeRemain / deltaTime;
            DirVector move = getVelocity();
            DirVector randDirection = DirVector.randomOrb();
            move.add(randDirection.mul(SEEKER_LAUNCH_SPEED));

            EntityState state = new EntityState(position, randDirection, move);

            target = sourceJet.getTarget(randDirection, getPosition(), entities);
            entityDeposit.addSpawn(new Seeker.Factory(state, timeFraction, sourceJet, target));

            seekerTimeRemain += SEEKER_COOLDOWN;
        } while (seekerTimeRemain < 0);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.rotate(renderTime(), 0, -1, 0);
            ms.scale(SCALE);
            action.accept(GeneralShapes.ICOSAHEDRON);
        }
        ms.popMatrix();
    }

    public static class Factory extends EntityFactory {
        private int sourceID;

        public Factory() {
        }

        public Factory(DeathIcosahedron e) {
            super(EntityClass.DEATHICOSAHEDRON, e);
            sourceID = e.sourceJet.idNumber();
        }

        public Factory(AbstractJet jet) {
            super(EntityClass.DEATHICOSAHEDRON, jet.getPosition(), new Quaternionf(), getVelocity(jet));
            sourceID = jet.idNumber();
        }

        public static DirVector getVelocity(AbstractJet jet) {
            DirVector vel = jet.getVelocity();
            vel.add(jet.getForward().scale(FIRE_SPEED));
            return vel;
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            MovingEntity entity = entities.getEntity(sourceID);
            return new DeathIcosahedron(id, position, rotation, velocity, game, game.getTimer(), entity, entities);
        }

        @Override
        protected void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeInt(sourceID);
        }

        @Override
        protected void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            sourceID = in.readInt();
        }
    }
}
