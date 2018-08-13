package nl.NG.Jetfightergame.AbstractEntities.Factory;

import nl.NG.Jetfightergame.AbstractEntities.InvisibleEntity;
import nl.NG.Jetfightergame.AbstractEntities.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.Assets.Entities.FallingCube;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetBasic;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.JetSpitsy;
import nl.NG.Jetfightergame.Assets.Entities.OneHitShield;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.*;
import nl.NG.Jetfightergame.Assets.Entities.ReflectorShield;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 * @see EntityFactory for how to create entities
 */
public enum EntityClass {
    JET_BASIC(JetBasic.Factory::new),
    JET_SPITZ(JetSpitsy.Factory::new),

    FALLING_CUBE(FallingCube.Factory::new),
    SIMPLE_BULLET(SimpleBullet.Factory::new),
    INVISIBLE_ENTITY(InvisibleEntity.Factory::new),
    POWERUP(PowerupEntity.Factory::new),

    SIMPLE_ROCKET(SimpleRocket.Factory::new),
    SEEKER(Seeker.Factory::new),
    DEATHICOSAHEDRON(DeathIcosahedron.Factory::new),
    CLUSTER_ROCKET(ClusterRocket.Factory::new),
    ONEHIT_SHIELD(OneHitShield::newFactory),
    REFLECTOR_SHIELD(ReflectorShield::newFactory);

    private static final EntityClass[] VALUES = values();
    private final Supplier<EntityFactory> constructor;

    EntityClass(Supplier<EntityFactory> constructor) {
        this.constructor = constructor;
    }

    /**
     * @param id a number n corresponing to an enum ordinal
     * @return the enum e such that {@code e.ordinal() == n}
     * @throws IllegalArgumentException if the id does not correspond to a valid message
     */
    public static EntityClass get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid entityclass identifier " + id);
        else return VALUES[id];
    }

    public static String asString(int id) {
        return id < VALUES.length ? get(id).toString() : id + " (Invalid entity id)";
    }

    EntityFactory getFactory() {
        EntityFactory f = constructor.get();
        f.type = this;
        return f;
    }
}
