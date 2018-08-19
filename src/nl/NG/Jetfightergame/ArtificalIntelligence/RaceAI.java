package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 19-8-2018.
 */
public class RaceAI extends RocketAI {
    private static final float SHOOT_ACCURACY = 0.05f;
    private static final float SAFE_DIST = 100f;
    private static final float POWERUP_COLLECT_BENDOUT = 0.2f;
    private static final float POWERUP_LOOK_COOLDOWN = 4f;

    private final RaceProgress race;
    private final Player player;
    private final AbstractJet jet;
    private final EntityMapping entities;

    /**
     * a controller that follows the course of the race
     * @param player   the jet that is controlled by this controller
     * @param entities the entities of this game
     */
    public RaceAI(Player player, RaceProgress race, EntityMapping entities) {
        super(player.jet(), null, 100f, 1f, 1f, 1f);
        this.entities = entities;
        this.jet = player.jet();
        vecToTarget = DirVector.zeroVector();

        this.player = player;
        this.race = race;
    }

    @Override
    public void update() {
        target = nextPoint();

        if (Toolbox.random.nextFloat() < POWERUP_LOOK_COOLDOWN / ServerSettings.TARGET_TPS) {
            target = getPowerupOnPath(target);
        }

        if (target == null) {
            xVec = projectile.relativeStateDirection(DirVector.xVector());
            yVec = projectile.relativeStateDirection(DirVector.yVector());
            zVec = projectile.relativeStateDirection(DirVector.zVector());
            vecToTarget = xVec;
            return;
        }

        super.update();
    }

    private Touchable nextPoint() {
        int pInd = race.getPlayerInd(player);
        if (pInd < 0) return null;
        return race.nextPointEntityOf(pInd, 2);
    }

    @Override
    public boolean primaryFire() {
        switch (jet.getCurrentPowerup()) {
            case NONE:
                return false;

            case STAR_BOOST:
            case SPEED_BOOST:
            case BLACK_HOLE:
                return true;

            case ROCKET:
            case GRAPPLING_HOOK:
            case DEATHICOSAHEDRON:
                return xVec.dot(vecToTarget) > (1 - SHOOT_ACCURACY);

            default:
                return shouldWorry();
        }
    }

    private Touchable getPowerupOnPath(Touchable defaultTarget) {
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

                DirVector vecToEnemy = jetPos.to(pop.getPosition(), new DirVector());
                float dist = vecToEnemy.lengthSquared();
                if (dist > vecToTarget.lengthSquared() / 2) continue;
                if (vecToTarget.dot(vecToEnemy) > POWERUP_COLLECT_BENDOUT) continue;

                if (dist < min) {
                    min = dist;
                    thing = entity;
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
