package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 23-7-2018.
 */
public class HunterAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;

    private final AbstractJet jet;
    private final Touchable actualTarget;
    private final EntityMapping entities;

    /**
     * a controller that tries to collect powerups and hunt down the player
     * @param jet             the jet that is controlled by this controller
     * @param target          the target entity that this projectile tries to hunt down
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given jet
     */
    public HunterAI(AbstractJet jet, Touchable target, EntityMapping entities, float projectileSpeed) {
        super(jet, target, projectileSpeed, 10f);
        this.jet = jet;
        this.actualTarget = target;
        this.entities = entities;
    }

    @Override
    public void update() {
        PowerupType currPop = jet.getCurrentPowerup();

        if (target == null) {
            target = getClosestPowerup(actualTarget);

        } else if (!target.equals(actualTarget) && !isNotAssault(currPop)) {
            this.target = actualTarget;

        } else if (target.equals(actualTarget) && isNotAssault(currPop)) {
            this.target = getClosestPowerup(actualTarget);
        }

        if (target instanceof PowerupEntity) {
            PowerupEntity pop = (PowerupEntity) target;
            if (pop.isCollected()) this.target = getClosestPowerup(actualTarget);
        }

        super.update();
    }

    private Touchable getClosestPowerup(Touchable defaultTarget) {
        float min = Float.MAX_VALUE;
        Touchable thing = defaultTarget;
        PosVector jetPos = jet.getPosition();
        PowerupType currPop = jet.getCurrentPowerup();

        for (MovingEntity entity : entities) {
            if (entity instanceof PowerupEntity) {
                PowerupEntity pop = (PowerupEntity) entity;
                if (pop.isCollected()) continue;

                PowerupType newPop = currPop.with(pop.getPowerupType());
                if (newPop == PowerupType.NONE || newPop == currPop) continue;

                float dist = entity.getPosition().distanceSquared(jetPos);
                if (dist < min) {
                    min = dist;
                    thing = entity;
                }
            }
        }

        return thing;
    }

    @Override
    public boolean primaryFire() {
        PowerupType currPop = jet.getCurrentPowerup();
        if (isNotAssault(currPop)) return false;
        return xVec.dot(vecToTarget) > (1 - SHOOT_ACCURACY);
    }

    private boolean isNotAssault(PowerupType currPop) {
        return currPop == PowerupType.NONE || currPop == PowerupType.SPEED_BOOST || currPop == PowerupType.SMOKE || currPop == PowerupType.SHIELD;
    }
}
