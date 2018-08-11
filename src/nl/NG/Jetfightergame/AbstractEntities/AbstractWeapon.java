package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
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
    public Collection<EntityFactory> update(float deltaTime, boolean isFiring, EntityState source, SpawnReceiver entityDeposit) {
        timeRemaining -= deltaTime;
        if (timeRemaining >= 0) return Collections.emptySet();

        List<EntityFactory> newProjectiles = new ArrayList<>();
        if (!wasFiring) timeRemaining = 0;

        if (isFiring) {
            do {
                final float timeFraction = timeRemaining / deltaTime;
                EntityFactory bullet = newProjectile(source, entityDeposit, timeFraction);
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
     * @param entityDeposit
     * @param timeFraction the deltaTime offset of spawning
     * @return a new projectile as if it was fired on the given moment
     */
    protected abstract EntityFactory newProjectile(EntityState source, SpawnReceiver entityDeposit, float timeFraction);
}
