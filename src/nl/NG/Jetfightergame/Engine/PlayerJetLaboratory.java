package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class PlayerJetLaboratory extends GameState {
    public PlayerJetLaboratory(Controller input) {
        super(input);
    }

    @Override
    protected void buildScene() {
        dynamicEntities.add(getPlayer());
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new ContainerCube(100));
        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
