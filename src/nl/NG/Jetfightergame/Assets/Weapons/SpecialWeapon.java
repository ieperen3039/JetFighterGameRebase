package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Assets.GeneralEntities.SimpleBullet;
import nl.NG.Jetfightergame.Engine.GameState.EntityManager;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public class SpecialWeapon extends AbstractWeapon {

    public SpecialWeapon(float cooldown) {
        super(cooldown);

    }

    @Override
    protected AbstractProjectile newProjectile(float spawnTime, GameEntity.State source, EntityManager entityDeposit) {
        DirVector vel = source.velocity();
        vel.add(source.forward().mul(20));
        final PosVector pos = source.position(spawnTime);
        final Quaternionf rot = source.rotation(spawnTime);
        return new SimpleBullet(pos, vel, rot, entityDeposit.getTimer(), entityDeposit, Identity.next());
    }
}
