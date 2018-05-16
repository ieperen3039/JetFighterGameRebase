package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.GameState.SpawnReceiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public abstract class AbstractWeapon implements Serializable {
    protected final float cooldown;
    /** time until next fire */
    protected float timeRemaining;
    private boolean wasFiring = true;

    /**
     * any gun that continues to shoot when fire-key is held down
     * @param timeBetweenShots seconds between two gunshots
     */
    public AbstractWeapon(float timeBetweenShots) {
        if (timeBetweenShots <= 0) throw new IllegalArgumentException("illegal reload time: " + timeBetweenShots);
        this.cooldown = timeBetweenShots;
        timeRemaining = 1f;
    }

    /**
     * @param deltaTime time since last check
     * @param isFiring true iff the controller wants to fire
     * @param source a description of what object to shoot
     * @param entityDeposit allows the new bullets to add entities
     *                      @return a collection of all newly generated entities
     */
    public Collection<MovingEntity.Spawn> update(float deltaTime, boolean isFiring, GameEntity.State source, SpawnReceiver entityDeposit) {
        timeRemaining -= deltaTime;
        if (timeRemaining >= 0) return Collections.EMPTY_SET;

        List<MovingEntity.Spawn> newProjectiles = new ArrayList<>();
        if (!wasFiring) timeRemaining = 0;

        float loopStart = entityDeposit.getTimer().time() - deltaTime;
        if (isFiring) {
            do {
                final float bulletSpawnTime = loopStart + timeRemaining;
                MovingEntity.Spawn bullet = newProjectile(bulletSpawnTime, source, entityDeposit);
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
    protected abstract MovingEntity.Spawn newProjectile(float spawnTime, GameEntity.State source, SpawnReceiver entityDeposit);
}