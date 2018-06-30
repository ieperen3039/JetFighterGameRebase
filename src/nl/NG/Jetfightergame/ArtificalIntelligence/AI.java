package nl.NG.Jetfightergame.ArtificalIntelligence;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.AbstractGameLoop;

/**
 * @author Geert van Ieperen
 * created on 9-2-2018.
 */
public abstract class AI extends AbstractGameLoop implements Controller {
    public AI(String name, int targetTps) {
        super(name, targetTps, false);
    }

    @Override
    public boolean isActiveController() {
        return false;
    }
}
