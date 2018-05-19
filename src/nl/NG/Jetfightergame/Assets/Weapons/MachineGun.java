package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractWeapon;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public class MachineGun extends AbstractWeapon {
    public MachineGun(float timeBetweenShots) {
        super(timeBetweenShots);
    }

    @Override
    protected MovingEntity.Spawn newProjectile(float spawnTime, GameEntity.State source, SpawnReceiver entityDeposit) {
        DirVector vel = source.velocity();
        vel.add(source.forward().mul(100));
        final PosVector pos = source.position(spawnTime);
        final Quaternionf rot = source.rotation(spawnTime);
        return new MovingEntity.Spawn(EntityClass.SIMPLE_BULLET, pos, rot, vel);
    }
}
