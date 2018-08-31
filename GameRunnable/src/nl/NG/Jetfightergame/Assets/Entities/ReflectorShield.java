package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Sound.MovingAudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

/**
 * a shield that persists for a fixed duration
 */
public class ReflectorShield extends AbstractShield {
    private static final float BURST_FORCE = 10f;
    private static final Color4f COLOR = new Color4f(1f, 0, 0, 0.1f);
    private EntityMapping entities;

    private ReflectorShield(int id, AbstractJet jet, GameTimer time, SpawnReceiver deposit, EntityMapping entities) {
        super(id, jet, time, PowerupType.REFLECTOR_DURATION, deposit);
        this.entities = entities;

        if (!deposit.isHeadless()) {
            deposit.add(new MovingAudioSource(Sounds.shield, this, 0.4f, 1.0f, true));
        }
    }

    @Override
    public void impact(float factor, float duration) {
        DirVector move = getVelocity();
        DirVector randDirection = DirVector.randomOrb();
        move.add(randDirection.mul(10f));

        EntityState state = new EntityState(position, randDirection, move);
        MovingEntity target = jet.getTarget(randDirection, getPosition(), entities);
        entityDeposit.add(new Seeker.Factory(state, 0, jet, target));
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.GLASS, COLOR);
        velocity = velocityAtRenderTime();
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    public static ShieldFactory newFactory() {
        return new Factory();
    }

    @Override
    public ParticleCloud explode() {
        return Particles.splitIntoParticles(this, BURST_FORCE, COLOR);
    }

    public static class Factory extends ShieldFactory {
        public Factory() {
        }

        public Factory(ReflectorShield shield) {
            super(EntityClass.REFLECTOR_SHIELD, shield);
        }

        public Factory(AbstractJet jet) {
            super(EntityClass.REFLECTOR_SHIELD, jet);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new ReflectorShield(id, getJet(entities), game.getTimer(), game, entities);
        }
    }
}
