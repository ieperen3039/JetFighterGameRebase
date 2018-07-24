package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupType;

/**
 * @author Geert van Ieperen. Created on 23-7-2018.
 */
public class HunterAI extends RocketAI {
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
        super(jet, target, projectileSpeed, true);
        this.jet = jet;
        actualTarget = target;
        this.entities = entities;
    }

    @Override
    public void update() {
        PowerupType currPop = jet.getCurrentPowerup();
        if (getTarget() != actualTarget && currPop != PowerupType.NONE) {
            if (currPop == PowerupType.SPEED || currPop == PowerupType.SMOKE) {
                setTarget(getClosestPowerup());
            } else {
                setTarget(actualTarget);
            }

        } else if (getTarget() == actualTarget && currPop == PowerupType.NONE) {
            setTarget(getClosestPowerup());
        }

        if (getTarget() instanceof PowerupEntity) {
            PowerupEntity pop = (PowerupEntity) getTarget();
            if (pop.isCollected()) setTarget(getClosestPowerup());
        }

        super.update();
    }

    private MovingEntity getClosestPowerup() {
        float min = Float.MAX_VALUE;
        MovingEntity thing = actualTarget;

        for (MovingEntity entity : entities) {
            if (entity instanceof PowerupEntity) {
                PowerupEntity pop = (PowerupEntity) entity;

                if (!pop.isCollected()) {
                    float dist = jet.getPosition().sub(entity.getPosition()).lengthSquared();

                    if (dist < min) {
                        min = dist;
                        thing = entity;
                    }
                }
            }
        }
        return thing;
    }
}
