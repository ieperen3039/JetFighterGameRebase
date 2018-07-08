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

        PosVector position = new PosVector();
        DirVector direction = new DirVector(1, 1, 0.2f);
        for (int i = 1; i < 15; i++) {
            position.add(direction.reducedTo(70, new DirVector()));
            direction.rotateZ((float) Math.PI / 4);
            entities.add(raceProgress.addCheckpoint(
                    new PosVector(position), new DirVector(direction), 20, Color4f.BLUE
            ));
        }

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.ROUGH, new Color4f(0.2f, 0.4f, 0.2f), new PosVector(0, 0, -250)));
        }

        return entities;
    }

    @Override
    protected Collection<Prentity> getInitialEntities() {
        return Collections.EMPTY_SET;
    }

    @Override
    public MovingEntity.State getNewSpawnPosition() {
        DirVector direction = new DirVector(1, 1, 0);
        direction.normalize();
        return new MovingEntity.State(PosVector.zeroVector(), direction, DirVector.zeroVector());
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.6f, 0.6f, 0.9f);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
