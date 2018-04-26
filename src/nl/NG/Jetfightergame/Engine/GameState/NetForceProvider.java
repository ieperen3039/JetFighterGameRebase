package nl.NG.Jetfightergame.Engine.GameState;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public interface NetForceProvider {
    /**
     * @param entity any dynamic entity in this world
     * @return the force of gravity (or whatever) that naturally acts on this entity
     */
    DirVector entityNetforce(MovingEntity entity);
}
