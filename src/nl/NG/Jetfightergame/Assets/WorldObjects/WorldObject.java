package nl.NG.Jetfightergame.Assets.WorldObjects;

import nl.NG.Jetfightergame.AbstractEntities.Touchable;

/**
 * @author Geert van Ieperen created on 19-6-2018.
 */
public interface WorldObject extends Touchable {
    /**
     * apply shape modifications after the world is put together
     */
    void postWeldProcessing();
}
