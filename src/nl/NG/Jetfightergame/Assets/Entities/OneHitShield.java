package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * a shield that pops at the first hit
 */
public class OneHitShield extends AbstractShield {
    private static final float BURST_FORCE = 3f;
    public static final Color4f COLOR = new Color4f(0, 0, 1, 0.1f);

    private OneHitShield(int id, AbstractJet jet, GameTimer time, SpawnReceiver deposit) {
        super(id, jet, time, PowerupType.OHSHIELD_DURATION, deposit);
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.GLASS, COLOR);
    }

    @Override
    public void impact(float factor, float duration) {
        timeToLive = 0;
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

        public Factory(OneHitShield shield) {
            super(EntityClass.ONEHIT_SHIELD, shield);
        }

        public Factory(AbstractJet jet) {
            super(EntityClass.ONEHIT_SHIELD, jet);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new OneHitShield(id, getJet(entities), game.getTimer(), game);
        }

    }
}
