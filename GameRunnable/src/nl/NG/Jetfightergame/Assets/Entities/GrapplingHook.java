package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Sound.MovingAudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 13-8-2018.
 */
public class GrapplingHook extends AbstractProjectile {
    private static final float EIGHTSTH = (float) (Math.PI / 4);

    private static final float MAX_FLY_DURATION = 2f;
    private static final float RATTLE_PITCH = 0.5f;

    private static final float LOCK_LENGTH = 5f;
    private static final float LOCK_WIDTH = 0.3f;
    private static final Color4f COLOR = Color4f.GREY;

    private AbstractJet hookedOther = null;

    private GrapplingHook(
            int id, PosVector initialPosition, DirVector initialVelocity,
            GameTimer gameTimer, AbstractJet sourceEntity, SpawnReceiver particleDeposit,
            MovingEntity target) {
        super(
                id, initialPosition, Toolbox.xTo(initialVelocity), initialVelocity,
                0.01f, 0, MAX_FLY_DURATION, 20f, 0, 0, 0.95f,
                particleDeposit, gameTimer, sourceEntity
        );

        entityDeposit.add(getRattle(sourceEntity));
        this.target = target;
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
        other.impact(5f, 0.2f);

        if (other instanceof AbstractJet) {
            hookedOther = (AbstractJet) other;
            timeToLive = PowerupType.GRAPPLE_PULL_DURATION;

            addForce(sourceJet, hookedOther, PowerupType.GRAPPLE_YOUR_PULL_FORCE);
            addForce(hookedOther, sourceJet, PowerupType.GRAPPLE_HIS_PULL_FORCE);
        } else {
            timeToLive = 0;
        }
    }

    private void addForce(AbstractJet pulled, AbstractJet other, float magnitude) {
        pulled.addNetForce(PowerupType.GRAPPLE_PULL_DURATION, () -> {
            DirVector newForce = pulled.getVecTo(other);
            if (isOverdue()) return DirVector.zeroVector();
            newForce.reducedTo(magnitude, newForce);
            return newForce;
        });
    }

    @Override
    public void draw(GL2 gl) {
        PosVector headPos = interpolatedPosition();
        PosVector sourcePos = sourceJet.getPosition();
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
                gl.popMatrix();
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
    public void applyPhysics(DirVector netForce) {
        super.applyPhysics(netForce);
        if (hookedOther != null) {
            extraPosition = hookedOther.getExpectedMiddle();

            DirVector vecToOther = sourceJet.getVecTo(hookedOther);
            if (sourceJet.getForward().dot(vecToOther) < 0) timeToLive = 0;
        }
    }

    public PosVector interpolatedPosition() {
        if (hookedOther != null) return hookedOther.getPosition();
        return getPosition();
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

    private MovingAudioSource getRattle(AbstractJet sourceEntity) {
        return new MovingAudioSource(Sounds.windOff, sourceEntity, RATTLE_PITCH, 0.5f, false) {
            @Override
            public boolean isOverdue() {
                return (hookedOther != null) && (GrapplingHook.this.timeToLive > 0);
            }
        };
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
                jet.getVelocity().add(jet.getForward().reducedTo(PowerupType.GRAPPLE_FIRE_SPEED, dest), dest);
                return dest;
            }

            PosVector thisPos = jet.getPosition();

            PosVector tgtPos = RocketAI.extrapolateTarget(tgt.getVelocity(), tgt.getPosition(), thisPos, PowerupType.GRAPPLE_FIRE_SPEED);
            DirVector vecToTarget = thisPos.to(tgtPos, new DirVector());
            vecToTarget.reducedTo(PowerupType.GRAPPLE_FIRE_SPEED, vecToTarget);
            return vecToTarget;
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, AbstractJet src, MovingEntity tgt) {
            return new GrapplingHook(id, position, getVelocity(src, tgt), game.getTimer(), src, game, tgt);
        }
    }
}
