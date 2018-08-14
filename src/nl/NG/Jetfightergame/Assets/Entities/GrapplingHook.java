package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 13-8-2018.
 */
public class GrapplingHook extends AbstractProjectile {
    private static final float YOUR_PULL_FORCE = 2000f;
    private static final float HIS_PULL_FORCE = 500f;

    private static final float PULL_DURATION = 3f;
    private static final float FIRE_SPEED = 200f;
    public static final float EIGHTSTH = (float) (Math.PI / 4);
    public static final float MAX_FLY_DURATION = 2f;

    private static final float LOCK_LENGTH = 5f;
    public static final float LOCK_WIDTH = 0.3f;
    public static final Color4f COLOR = Color4f.GREY;
    private static final float TARGET_PULL_FORCE_FACTOR = 3_000f;

    private AbstractJet hookedOther = null;

    public GrapplingHook(
            int id, PosVector initialPosition, DirVector initialVelocity,
            GameTimer gameTimer, AbstractJet sourceEntity, SpawnReceiver particleDeposit,
            MovingEntity target) {
        super(
                id, initialPosition, Toolbox.xTo(initialVelocity), initialVelocity,
                0.01f, 0, MAX_FLY_DURATION, 20f, 0, 0, 0.95f,
                particleDeposit, gameTimer, sourceEntity
        );
        this.target = target;

        addNetForce(timeToLive, () -> {
            if (this.target == null) return new DirVector();
            DirVector vecToTarget = getVecTo(this.target);
            vecToTarget.div(vecToTarget.lengthSquared() / TARGET_PULL_FORCE_FACTOR);
            return vecToTarget;
        });
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        if (hookedOther != null) return null;

        if (super.checkCollisionWith(other, deltaTime) != null) {
            collideWithOther(other);
        }

        return null;
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(0.1f);

        if (other instanceof AbstractJet) {
            hookedOther = (AbstractJet) other;
            timeToLive = PULL_DURATION;

            addForce(sourceJet, hookedOther, YOUR_PULL_FORCE);
            addForce(hookedOther, sourceJet, HIS_PULL_FORCE);
        } else {
            timeToLive = 0;
        }
    }

    private void addForce(AbstractJet pulled, AbstractJet other, float magnitude) {
        pulled.addNetForce(PULL_DURATION, () -> {
            DirVector newForce = pulled.getVecTo(other);
            if (isOverdue()) return DirVector.zeroVector();
            newForce.reducedTo(magnitude, newForce);
            return newForce;
        });
    }

    @Override
    public void draw(GL2 gl) {
        PosVector headPos = interpolatedPosition();
        PosVector sourcePos = sourceJet.interpolatedPosition();
        DirVector vecFromTarget = headPos.to(sourcePos, new DirVector());

        gl.setMaterial(Material.SILVER, COLOR);
        gl.pushMatrix();
        {
            gl.pointFromTo(headPos, sourcePos);
            gl.scale(LOCK_WIDTH, LOCK_WIDTH, LOCK_LENGTH / 2);
            gl.translate(0, 0, 1);

            float vecLength = vecFromTarget.length();
            if (vecLength > 1000) {
                timeToLive = 0;
                return;
            }

            float nOfLocks = vecLength / LOCK_LENGTH - 1;
            for (int i = 0; i < nOfLocks; i++) {
                gl.draw(GeneralShapes.CUBE);
                gl.translate(0, 0, 2);
                gl.rotate(EIGHTSTH, 0, 0, 1);
            }
        }
        gl.popMatrix();
    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        super.applyPhysics(netForce, deltaTime);
        if (hookedOther != null) {
            extraPosition = hookedOther.getExpectedMiddle();

            DirVector vecToOther = sourceJet.getVecTo(hookedOther);
            if (sourceJet.getForward().dot(vecToOther) < 0) timeToLive = 0;
        }
    }

    @Override
    public PosVector interpolatedPosition() {
        if (hookedOther != null) return hookedOther.interpolatedPosition();
        return super.interpolatedPosition();
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public void preDraw(GL2 gl) {
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(AbstractJet source, MovingEntity target) {
            super(EntityClass.GRAPPLING_HOOK, source.getState(), 0, source, target);
        }

        public Factory(GrapplingHook hook) {
            super(EntityClass.GRAPPLING_HOOK, hook);
        }

        private static DirVector getVelocity(AbstractJet jet, MovingEntity tgt) {

            if (tgt == null) {
                DirVector dest = new DirVector();
                jet.getVelocity().add(jet.getForward().reducedTo(FIRE_SPEED, dest), dest);
                return dest;
            }

            PosVector thisPos = jet.getPosition();

            PosVector tgtPos = RocketAI.extrapolateTarget(tgt.getVelocity(), tgt.getPosition(), thisPos, FIRE_SPEED);
            DirVector vecToTarget = thisPos.to(tgtPos, new DirVector());
            vecToTarget.reducedTo(FIRE_SPEED, vecToTarget);
            return vecToTarget;
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, AbstractJet src, MovingEntity tgt) {
            return new GrapplingHook(id, position, getVelocity((AbstractJet) src, tgt), game.getTimer(), src, game, tgt);
        }
    }
}
