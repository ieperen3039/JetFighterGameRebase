package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Engine.PathDescription;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public class EntityList implements EntityManagement {
    private final Collection<MovingEntity> dynamicEntities;
    private final Collection<Touchable> staticEntities;

    public EntityList(Collection<Touchable> staticEntities) {
        this.dynamicEntities = new CopyOnWriteArrayList<>();
        this.staticEntities = Collections.unmodifiableCollection(staticEntities);
    }

    @Override
    public void preUpdateEntities(NetForceProvider gravity, float deltaTime) {
        for (MovingEntity entity : dynamicEntities) {
            DirVector netForce = gravity.entityNetforce(entity);
            entity.preUpdate(deltaTime, netForce);
        }
    }

    @Override
    public void analyseCollisions(float currentTime, float deltaTime, PathDescription path) {
        // do nothing
    }

    @Override
    public void addEntity(MovingEntity entity) {
        dynamicEntities.add(entity);
    }

    @Override
    public void addEntities(Collection<? extends MovingEntity> newEntities) {
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

    @Override
    public void removeEntity(MovingEntity entity) {
        dynamicEntities.remove(entity);
        assert getDynamicEntities().stream().noneMatch(e -> e.idNumber() == entity.idNumber()) : "Could not remove entity " + entity;
    }

    @Override
    public PosVector rayTrace(PosVector from, PosVector to) {
        float xMin = Math.min(from.x, to.x);
        float yMin = Math.min(from.y, to.y);
        float zMin = Math.min(from.z, to.z);
        float xMax = Math.max(from.x, to.x);
        float yMax = Math.max(from.y, to.y);
        float zMax = Math.max(from.z, to.z);
        Collision minColl = null;

        for (Touchable entity : staticEntities) {
            PosVector eMid = entity.getExpectedMiddle();
            float range = entity.getRange();
            if (eMid.x + range < xMin || eMid.x - range > xMax) continue;
            if (eMid.y + range < yMin || eMid.y - range > yMax) continue;
            if (eMid.z + range < zMin || eMid.z - range > zMax) continue;

            Collision coll = MovingEntity.getPointCollision(null, entity, from, to, 0);
            if (coll == null) continue;

            if (minColl == null || coll.compareTo(minColl) < 0) {
                minColl = coll;
            }
        }

        return minColl == null ? to : minColl.hitPosition();
    }
}
