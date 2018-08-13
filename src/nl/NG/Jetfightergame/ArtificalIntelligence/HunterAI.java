package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 23-7-2018.
 */
public class HunterAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;

    private final AbstractJet jet;
    private final MovingEntity actualTarget;
    private final EntityMapping entities;

    /**
     * a controller that tries to collect powerups and hunt down the player
     * @param jet             the jet that is controlled by this controller
     * @param target          the target entity that this projectile tries to hunt down
     * @param projectileSpeed the assumed (and preferably over-estimated) maximum speed of the given jet
     */
    public HunterAI(AbstractJet jet, MovingEntity target, EntityMapping entities, float projectileSpeed) {
        super(jet, target, projectileSpeed, 10f);
        this.jet = jet;
        actualTarget = target;
        this.entities = entities;
    }

    @Override
    public void update() {
        PowerupType currPop = jet.getCurrentPowerup();

        if (!target.equals(actualTarget) && !isNotAssault(currPop)) {
            this.target = actualTarget;

        } else if (target.equals(actualTarget) && isNotAssault(currPop)) {
            this.target = getClosestPowerup();
        }

        if (target instanceof PowerupEntity) {
            PowerupEntity pop = (PowerupEntity) target;
            if (pop.isCollected()) this.target = getClosestPowerup();
        }

        super.update();
    }

    private MovingEntity getClosestPowerup() {
        float min = Float.MAX_VALUE;
        MovingEntity thing = actualTarget;
        PosVector jetPos = jet.getPosition();

        for (MovingEntity entity : entities) {
            if (entity instanceof PowerupEntity) {
                PowerupEntity pop = (PowerupEntity) entity;

                if (pop.isCollected()) continue;
                PowerupType newPop = jet.getCurrentPowerup().with(pop.getPowerupType());
                if (newPop == PowerupType.NONE) continue;

                float dist = entity.getPosition().sub(jetPos).lengthSquared();

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
        return currPop == PowerupType.NONE || currPop == PowerupType.SPEED || currPop == PowerupType.SMOKE || currPop == PowerupType.SHIELD;
    }
}
