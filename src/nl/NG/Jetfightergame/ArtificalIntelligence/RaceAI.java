package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 19-8-2018.
 */
public class RaceAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;
    private static final float SAFE_DIST = 200f;
    private static final float POWERUP_COLLECT_BENDOUT = (float) Math.cos(0.3);
    private static final int LOOK_AHEAD = 3;

    private final RaceProgress race;
    private final Player player;
    private final AbstractJet jet;
    private final EntityMapping entities;
    private PowerupEntity powerupTarget;

    /**
     * a controller that follows the course of the race
     * @param player   the jet that is controlled by this controller
     * @param entities the entities of this game
     */
    public RaceAI(Player player, RaceProgress race, EntityMapping entities) {
        super(player.jet(), null, 300f, 1f, 2f, 1.5f);
        this.entities = entities;
        this.jet = player.jet();
        this.player = player;
        this.race = race;
        this.powerupTarget = null;
    }

    @Override
    public void update() {
        Touchable p = nextPoint();

        if (p == null) {
            xVec = projectile.relativeStateDirection(DirVector.xVector());
            yVec = projectile.relativeStateDirection(DirVector.yVector());
            zVec = projectile.relativeStateDirection(DirVector.zVector());
            vecToTarget = xVec;
            return;
        }

        if (powerupTarget == null || powerupTarget.isCollected()) {
            powerupTarget = getPowerupOnPath(p.getExpectedMiddle());

        } else if (vecTo(powerupTarget).dot(jet.getVelocity()) < 0) {
            powerupTarget = null;
        }

        target = (powerupTarget == null) ? p : powerupTarget;

        super.update();
    }

    private DirVector vecTo(Touchable p) {
        return projectile.getPosition().to(p.getExpectedMiddle(), new DirVector());
    }

    private Touchable nextPoint() {
        int pInd = race.getPlayerInd(player);
        if (pInd < 0) return null;
        return race.nextPointEntityOf(pInd, LOOK_AHEAD);
    }

    @Override
    public boolean primaryFire() {
        switch (jet.getCurrentPowerup()) {
            case NONE:
            case SMOKE:
                return false; // never

            case STAR_BOOST:
            case SPEED_BOOST:
            case BLACK_HOLE:
                return true; // always

            case ROCKET:
            case GRAPPLING_HOOK:
            case DEATHICOSAHEDRON:
                MovingEntity tgt = jet.getTarget();
                if (tgt == null) return false;

                DirVector toTgt = jet.getVecTo(tgt);
                return xVec.dot(toTgt.normalize()) > (1 - SHOOT_ACCURACY);

            default:
                return shouldWorry();
        }
    }

    private PowerupEntity getPowerupOnPath(PosVector targetPos) {
        float min = Float.MAX_VALUE;
        PowerupEntity thing = null;
        PosVector jetPos = jet.getPosition();
        PowerupType currPop = jet.getCurrentPowerup();
        DirVector vecToTarget = jetPos.to(targetPos, new DirVector());
        DirVector velocityNorm = jet.getVelocity().normalize(new DirVector());

        for (MovingEntity entity : entities) {
            if (entity instanceof PowerupEntity) {
                PowerupEntity pop = (PowerupEntity) entity;
                if (pop.isCollected()) continue;

                PowerupType newPop = currPop.with(pop.getPowerupType());
                if (newPop == PowerupType.NONE || newPop == currPop) continue;

                // pop is behind
                PosVector popPos = pop.getPosition();
                DirVector vecToPop = jetPos.to(popPos, new DirVector());
                if (velocityNorm.dot(vecToPop) < 0) continue;

                // (a2 + b2 > c2) => (corner jet-pop-tgt > 90 deg)
                float jetToPopDistSq = vecToPop.lengthSquared();
                float popToTgtDistSq = popPos.distanceSquared(targetPos);
                if (jetToPopDistSq + popToTgtDistSq > vecToTarget.lengthSquared()) continue;

                // (1 - corner jet-extrajet-pop) < cosh(BENDOUT)
                if (velocityNorm.dot(vecToPop.normalize()) < POWERUP_COLLECT_BENDOUT) continue;

                if ((jetToPopDistSq + popToTgtDistSq) < min) {
                    min = jetToPopDistSq;
                    thing = pop;
                }
            }
        }

        return thing;
    }

    private boolean shouldWorry() {
        PosVector jetPos = this.jet.getPosition();

        for (Player p : race.players()) {
            AbstractJet otherJet = p.jet();
            if (this.jet.equals(otherJet)) continue;

            DirVector vecToEnemy = jetPos.to(otherJet.getPosition(), new DirVector());
            if (vecToEnemy.lengthSquared() > SAFE_DIST * SAFE_DIST) continue;
            if (xVec.dot(vecToEnemy) < 0) return true; // is behind
        }

        return false;
    }
}
