package nl.NG.Jetfightergame.AbstractEntities.Factories;

import nl.NG.Jetfightergame.Assets.Entities.FallingCube;
import nl.NG.Jetfightergame.Assets.Entities.FighterJets.BasicJet;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.Seeker;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.SimpleBullet;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.SimpleRocket;
import nl.NG.Jetfightergame.Assets.Powerups.PowerupEntity;

import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 * @see EntityFactory for how to create entities
 */
public enum EntityClass {
    BASIC_JET(BasicJet.Factory::new),
    SIMPLE_BULLET(SimpleBullet.Factory::new),
    FALLING_CUBE(FallingCube.Factory::new),
    SIMPLE_ROCKET(SimpleRocket.Factory::new),
    SEEKER(Seeker.Factory::new),
    POWERUP(PowerupEntity.Factory::new);

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
