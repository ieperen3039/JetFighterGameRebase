package nl.NG.Jetfightergame.EntityGeneral.Powerups;

import nl.NG.Jetfightergame.Assets.Entities.AbstractProjectile;
import nl.NG.Jetfightergame.Assets.Entities.ClusterRocket;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Entities.Seeker;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.InvisibleEntity;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Function;

import static nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor.*;

/**
 * @author Geert van Ieperen. Created on 11-7-2018.
 */
public enum PowerupType {
    NONE(PowerupColor.NONE),

    ROCKET(RED),
    DEATHICOSAHEDRON(RED, BLUE),
    GRAPPLING_HOOK(RED, GREEN),
    SEEKERS(RED, YELLOW),

    SHIELD(BLUE),
    STAR_BOOST(BLUE, GREEN),
    REFLECTOR_SHIELD(BLUE, YELLOW),

    SPEED_BOOST(GREEN),
    BLACK_HOLE(GREEN, YELLOW),

    SMOKE(YELLOW);

    public static final float REFLECTOR_DURATION = 5f;
    private static final PowerupType[] VALUES = values();

    public static final float SEEKER_LAUNCH_SPEED = 2f;
    public static final int NOF_SEEKERS_LAUNCHED = 20;

    public static final float SPEED_BOOST_DURATION = 3f;
    public static final float SPEED_BOOST_FACTOR = 3f;

    public static final float OHSHIELD_DURATION = 2f;

    public static final float STAR_BOOST_DURATION = 15f;
    public static final float STAR_BOOST_FACTOR = 1.3f;
    public static final float STAR_BOOST_PUSH = 100_000f;

    public static final float SMOKE_LINGER_TIME = 30f;
    public static final float SMOKE_LAUNCH_SPEED = 20f;
    public static final float SMOKE_SPREAD = 10f;
    public static final int SMOKE_DENSITY = 1_000;
    public static final int SMOKE_DISTRACTION_ELEMENTS = 3;

    public static final float GRAPPLE_YOUR_PULL_FORCE = 1500f;
    public static final float GRAPPLE_HIS_PULL_FORCE = 400f;
    public static final float GRAPPLE_PULL_DURATION = 3f;
    public static final float GRAPPLE_FIRE_SPEED = 300f;

    private final EnumSet<PowerupColor> required;

    PowerupType(PowerupColor r, PowerupColor... o) {
        required = EnumSet.of(r, o);
    }

    /**
     * finds a powerup with the given type
     * @param resources the resources
     * @return the powerup that encapsulates exactly the given primitives
     */
    public static PowerupType get(EnumSet<PowerupColor> resources) {
        if (resources.size() == 1) return get(resources.iterator().next());

        for (PowerupType pwr : VALUES) {
            if (pwr.required.containsAll(resources)) {
                if (resources.containsAll(pwr.required)) {
                    return pwr;
                }
            }
        }
        return NONE;
    }

    public static PowerupType get(PowerupColor source) {
        switch (source) {
            case GREEN:
                return SPEED_BOOST;
            case YELLOW:
                return SMOKE;
            case RED:
                return ROCKET;
            case BLUE:
                return SHIELD;
            case NONE:
                return NONE;
        }

        throw new IllegalStateException();
    }

    public static PowerupType get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid power-up identifier " + id);
        else return VALUES[id];
    }

    public static void launchClusterRocket(AbstractJet jet, MovingEntity target, SpawnReceiver deposit) {
        EntityState spawnState = jet.getState();
        deposit.addSpawn(new ClusterRocket.Factory(spawnState, jet, target));
    }

    public static void launchSmokeCloud(AbstractJet jet, SpawnReceiver deposit) {
        DirVector dir = jet.getForward();
        dir.scale(-SMOKE_LAUNCH_SPEED).add(jet.getVelocity().scale(0.5f));
        deposit.addExplosion(
                jet.getPosition(), dir,
                Color4f.BLACK, Color4f.GREY,
                SMOKE_SPREAD, SMOKE_DENSITY, SMOKE_LINGER_TIME, 10f
        );
        // distraction
        for (int i = 0; i < SMOKE_DISTRACTION_ELEMENTS; i++) {
            DirVector move = new DirVector(dir);
            move.add(DirVector.random().scale(SMOKE_SPREAD / 10));
            deposit.addSpawn(new InvisibleEntity.Factory(jet.getPosition(), move, SMOKE_LINGER_TIME));
        }
    }

    public static void launchSeekers(AbstractJet jet, SpawnReceiver deposit, Function<EntityState, MovingEntity> target) {
        deposit.addSpawns(AbstractProjectile.createCloud(
                jet.getPosition(), jet.getVelocity(), NOF_SEEKERS_LAUNCHED, SEEKER_LAUNCH_SPEED,
                (state) -> new Seeker.Factory(state, Toolbox.random.nextFloat(), jet, target.apply(state))
        ));
    }

    public static void doStarBoost(AbstractJet jet, SpawnReceiver entityDeposit) {
        jet.addSpeedModifier(PowerupType.STAR_BOOST_FACTOR, STAR_BOOST_DURATION);
        entityDeposit.boosterColorChange(jet, Color4f.BLUE, Color4f.WHITE, STAR_BOOST_DURATION);
        entityDeposit.addGravitySource(jet::getExpectedMiddle, -STAR_BOOST_PUSH, STAR_BOOST_DURATION);
    }

    public PowerupType with(PowerupColor type) {
        if (this == NONE) return get(type);

        EnumSet<PowerupColor> types = required.clone();
        types.add(type);
        return get(types);
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", " ");
    }

    public Collection<PowerupColor> getRequiredResources() {
        return required.clone();
    }
}
