package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractWeapon;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Prentity;
import nl.NG.Jetfightergame.Assets.Entities.Projectiles.SimpleRocket;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 * @deprecated should function through powerup
 */
public class SpecialWeapon extends AbstractWeapon {

    public SpecialWeapon(float cooldown) {
        super(cooldown);
    }

    @Override
    protected Prentity newProjectile(MovingEntity.State source, SpawnReceiver entityDeposit, float timeFraction) {
        DirVector vel = source.velocity();
        final PosVector pos = source.position(timeFraction);
        final Quaternionf rot = source.rotation(timeFraction);
        return new Prentity(SimpleRocket.TYPE, pos, rot, vel);
    }
}
