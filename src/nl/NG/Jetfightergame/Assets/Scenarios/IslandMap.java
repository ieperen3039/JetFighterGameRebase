package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Prentity;
import nl.NG.Jetfightergame.AbstractEntities.StaticEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class IslandMap extends GameState {
    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress) {
        List<Touchable> entities = new ArrayList<>();

        for (int i = 1; i < 7; i++) {
            entities.add(raceProgress.addCheckpoint(
                    new PosVector(40 * i, 0, 10 * i), DirVector.xVector(), 10, Color4f.BLUE
            ));
        }

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.ROUGH, new Color4f(0.2f, 0.5f, 0.2f), new PosVector(0, 0, -200)));
        }

        return entities;
    }

    @Override
    protected Collection<Prentity> getInitialEntities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public MovingEntity.State getNewSpawnPosition() {
        return new MovingEntity.State();
    }

    @Override
    public Color4f fogColor() {
        return Color4f.WHITE;
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
