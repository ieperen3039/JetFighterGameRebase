package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public class EntityList implements EntityManagement {
    private final Collection<MovingEntity> dynamicEntities;
    private final Collection<Touchable> staticEntities;

    public EntityList(Collection<MovingEntity> dynamicEntities, Collection<Touchable> staticEntities) {
        this.dynamicEntities = new ArrayList<>(dynamicEntities);
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
    }

    @Override
    public void preUpdateEntities(NetForceProvider gameState, float deltaTime) {
        for (MovingEntity entity : dynamicEntities) {
            DirVector netForce = gameState.entityNetforce(entity);
            entity.preUpdate(deltaTime, netForce);
        }
    }

    @Override
    public void analyseCollisions(float currentTime, float deltaTime, PathDescription environment) {
        // do nothing
    }

    @Override
    public void addEntities(Collection<MovingEntity> newEntities) {
        dynamicEntities.addAll(newEntities);
    }

    @Override
    public Collection<Touchable> getStaticEntities() {
        return staticEntities;
    }

    @Override
    public Collection<MovingEntity> getDynamicEntities() {
        return Collections.unmodifiableCollection(dynamicEntities);
    }

    @Override
    public void updateEntities(float currentTime) {
        for (MovingEntity entity : dynamicEntities) {
            entity.update(currentTime);
        }
    }

    @Override
    public void cleanUp() {
        dynamicEntities.clear();
    }
}
