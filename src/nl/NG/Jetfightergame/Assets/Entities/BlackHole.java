package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
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
public class BlackHole extends AbstractProjectile {
    private static final float SPARK_COOLDOWN = 0.02f;
    private static final float FIRE_SPEED = 20f;
    public static final Color4f PARTICLE_COLOR_1 = Color4f.BLACK;
    public static final Color4f PARTICLE_COLOR_2 = new Color4f(0, 0, 0.2f);
    private static final float PARTICLE_REACH = 10f;
    private static final float PARTICLE_SIZE = 1f;
    private static final float TIME_TO_LIVE = 10f;
    private static final float YOUR_PULL_FORCE = 400f;
    private static final float OTHER_PULL_FORCE = 800f;

    private float sparkTimeRemain = 0;

    private BlackHole(
            int id, PosVector position, Quaternionf rotation, DirVector velocity,
            SpawnReceiver particleDeposit, GameTimer gameTimer, AbstractJet sourceEntity
    ) {
        super(
                id, position, rotation, velocity, 1f,
                0f, TIME_TO_LIVE, 0f, 0f, 0f, 0f,
                particleDeposit, gameTimer, sourceEntity
        );

        sourceJet.addNetForce(TIME_TO_LIVE, () -> getPullForce(sourceJet));

        particleDeposit.addGravitySource(this::getPosition, OTHER_PULL_FORCE, TIME_TO_LIVE);
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
        sparkTimeRemain -= gameTimer.getRenderTime().difference();
        if (sparkTimeRemain >= 0) return;

        ParticleCloud cloud = new ParticleCloud();
        do {
            DirVector move = DirVector.randomOrb();
            PosVector pos = interpolatedPosition();
            move.scale(PARTICLE_REACH);
            pos.sub(move);
            move.add(velocityAtRenderTime());

            Color4f color = PARTICLE_COLOR_1.interpolateTo(PARTICLE_COLOR_2, Toolbox.random.nextFloat());
            cloud.addParticle(pos, move, color, DirVector.random(), 2f, 1f, PARTICLE_SIZE);

            sparkTimeRemain += SPARK_COOLDOWN;
        } while (sparkTimeRemain < 0);

        entityDeposit.addParticles(cloud);
    }

    @Override
    public ParticleCloud explode() {
        timeToLive = 0;
        return null;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    private DirVector getPullForce(MovingEntity source) {
        DirVector newForce = source.getVecTo(this);
        if (source instanceof AbstractJet) { // should be the case
            AbstractJet sourceJet = (AbstractJet) this.sourceJet;
            if (newForce.dot(sourceJet.getForward()) > 0)
                newForce.reducedTo(YOUR_PULL_FORCE, newForce);
        }
        return newForce;
    }

    public static class Factory extends EntityFactory {
        private int sourceID;

        public Factory() {
        }

        public Factory(BlackHole e) {
            super(EntityClass.BLACK_HOLE, e);
            sourceID = e.sourceJet.idNumber();
        }

        public Factory(AbstractJet jet) {
            super(EntityClass.BLACK_HOLE, jet.getPosition(), new Quaternionf(), getVelocity(jet));
            sourceID = jet.idNumber();
        }

        public static DirVector getVelocity(AbstractJet jet) {
            DirVector vel = jet.getVelocity();
            DirVector launch = jet.getForward().scale(FIRE_SPEED);
            vel.add(launch);
            return vel;
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            AbstractJet entity = (AbstractJet) entities.getEntity(sourceID);
            return new BlackHole(id, position, rotation, velocity, game, game.getTimer(), entity);
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
