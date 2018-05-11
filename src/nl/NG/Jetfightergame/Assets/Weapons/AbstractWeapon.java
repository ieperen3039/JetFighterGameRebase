package nl.NG.Jetfightergame.Assets.Weapons;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.GameState.EntityReceiver;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public abstract class AbstractWeapon implements Serializable {
    protected final float cooldown;
    /** time until next fire */
    protected float timeRemaining;
    private boolean wasFiring = false;

    /**
     * any gun that continues to shoot when fire-key is held down
     * @param timeBetweenShots seconds between two gunshots
     */
    public AbstractWeapon(float timeBetweenShots) {
        if (timeBetweenShots <= 0) throw new IllegalArgumentException("illegal reload time: " + timeBetweenShots);
        this.cooldown = timeBetweenShots;
        timeRemaining = 0;
    }

    /**
     * @param deltaTime time since last check
     * @param isFiring true iff the controller wants to fire
     * @param source
     * @param entityDeposit allows the new bullets to add entities
     *                      @return a collection of all newly generated entities
     */
    public Collection<AbstractProjectile> update(float deltaTime, boolean isFiring, GameEntity.State source, EntityReceiver entityDeposit) {
        timeRemaining -= deltaTime;

        List<AbstractProjectile> newProjectiles = new ArrayList<>();

        if (timeRemaining >= 0) return newProjectiles;
        if (!wasFiring) timeRemaining = 0;

        if (isFiring) {
            do {
                final float bulletSpawnTime = deltaTime + timeRemaining;
                AbstractProjectile bullet = newProjectile(bulletSpawnTime, source, entityDeposit);
                bullet.preUpdate(-timeRemaining, DirVector.zeroVector());
                newProjectiles.add(bullet);
                timeRemaining += cooldown;
            } while (timeRemaining < 0);

            wasFiring = true;
        } else {
            wasFiring = false;
        }

        return newProjectiles;
    }

    /**
     * generates a new projectile on position and rotation interpolated by the given functions
     * @param spawnTime the deltaTime offset of spawning
     * @param entityDeposit
     * @return a new projectile as if it was fired on the given moment
     */
    protected abstract AbstractProjectile newProjectile(float spawnTime, GameEntity.State source, EntityReceiver entityDeposit);
}
