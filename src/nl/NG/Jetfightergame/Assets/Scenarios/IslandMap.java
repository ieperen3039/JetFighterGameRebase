package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.StaticEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

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

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.ROUGH, Color4f.GREY));
        }

        return entities;
    }

    @Override
    protected Collection<Spawn> getInitialEntities() {
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
