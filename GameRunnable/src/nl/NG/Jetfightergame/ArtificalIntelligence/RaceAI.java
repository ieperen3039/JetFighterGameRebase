package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.StaticEntity;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen. Created on 19-8-2018.
 */
public class RaceAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;
    private static final float SAFE_DIST = 200f;
    private static final float POWERUP_COLLECT_BENDOUT = (float) (Math.PI / 16);
    private static final float OFF_TARGET_BENDOUT = (float) (Math.PI / 6);
    private static final int LOOK_AHEAD = 0;
    private static final float CHECKPOINT_MARGIN = 0.8f; // [0, 1]
    private static final float POWERUP_LOSE_ANGLE = 1f;

    private final RaceProgress race;
    private final Player player;
    private final EntityMapping entities;

    private PowerupEntity powerupTarget;
    private AbstractJet jet;
    private PosVector jetPosition;
    private int pInd;

    /**
     * a controller that follows the course of the race
     * @param player   the jet that is controlled by this controller
     * @param entities the entities of this game
     */
    public RaceAI(Player player, RaceProgress race, EntityMapping entities) {
        super(player.jet(), null, 300f, 1f, 2f, 1.5f);
        this.entities = entities;
        this.player = player;
        this.race = race;
        this.powerupTarget = null;
    }

    @Override
    public void update() {
        this.jet = player.jet();
        jetPosition = jet.getPosition();
        target = nextPoint();
        pInd = race.getPlayerInd(player);

        if (target == null) {
            xVec = projectile.relativeDirection(DirVector.xVector());
            yVec = projectile.relativeDirection(DirVector.yVector());
            zVec = projectile.relativeDirection(DirVector.zVector());
            vecToTarget = xVec;
            return;
        }

        if (powerupTarget == null || powerupTarget.isCollected()) {
            if (pInd > 0) {
                StaticEntity ety = race.nextCheckpointOf(pInd, 2);
                powerupTarget = getPowerupOnPath(ety.getExpectedMiddle());
            }
        } else {
            DirVector vecToTarget = projectile.getPosition().to(powerupTarget.getExpectedMiddle(), new DirVector());
            if (vecToTarget.angle(jet.getVelocity()) > POWERUP_LOSE_ANGLE) {
                powerupTarget = null;
            }
        }

        target = (powerupTarget == null) ? target : powerupTarget;

        super.update();
    }

    @Override
    protected PosVector getTargetPosition() {
        if (target == null) {
            return new PosVector();

        } else if (target instanceof StaticEntity) {
            StaticEntity checkpoint = (StaticEntity) target;
            PosVector chPosition = checkpoint.getExpectedMiddle();

            DirVector forward = DirVector.xVector();
            forward.rotate(checkpoint.getRotation());
            DirVector vecToTgt = jetPosition.to(chPosition, new DirVector());
            DirVector down = forward.cross(vecToTgt, new DirVector());
            DirVector offset = forward.cross(down, down);

            offset.normalize();
            float proj = vecToTgt.negate().dot(offset); // scalar projection
            Vector3f vel = jet.getVelocity();
            if (vel.lengthSquared() > 500) vel.normalize(20);

            float maxOffsetLength = Math.min(CHECKPOINT_MARGIN * checkpoint.getRange(), 75);
            if (proj < maxOffsetLength) {
                offset.scale(proj);
                offset.add(forward.scale(20));
            } else {
                offset.scale(maxOffsetLength);
            }

            return chPosition.add(offset, new PosVector());
        }

        return target.getExpectedMiddle();
    }

    private StaticEntity nextPoint() {
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
        PowerupType currPop = jet.getCurrentPowerup();
        DirVector vecToTarget = jetPosition.to(targetPos, new DirVector());
        DirVector velocityNorm = jet.getVelocity().normalize(new DirVector());

        for (MovingEntity entity : entities) {
            if (entity instanceof PowerupEntity) {
                PowerupEntity pop = (PowerupEntity) entity;
                if (pop.isCollected()) continue;

                PowerupType newPop = currPop.with(pop.getPowerupType());
                if (newPop == PowerupType.NONE || newPop == currPop) continue;

                // pop is behind
                PosVector popPos = pop.getPosition();
                DirVector vecToPop = jetPosition.to(popPos, new DirVector());
                if (velocityNorm.dot(vecToPop) < 0) continue;
                if (vecToTarget.dot(vecToPop) < 0) continue;

                // (a2 + b2 > c2) => (corner jet-pop-tgt > 90 deg)
                float jetToPopDistSq = vecToPop.lengthSquared();
                float popToTgtDistSq = popPos.distanceSquared(targetPos);
                if (jetToPopDistSq + popToTgtDistSq > vecToTarget.lengthSquared()) continue;

                // (1 - corner jet-extrajet-pop) < cosh(BENDOUT)
                if (velocityNorm.dot(vecToPop.normalize()) < 1 - POWERUP_COLLECT_BENDOUT) continue;
                if (vecToTarget.normalize().dot(vecToPop) < 1 - OFF_TARGET_BENDOUT) continue;

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
