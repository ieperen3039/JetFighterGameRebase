package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.StaticEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class IslandMap extends GameState {
    private static final int FOG_DIST = 500;
    private PosVector nextSpawnPosition = PosVector.zeroVector();

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        List<Touchable> entities = new ArrayList<>();

        PosVector position = new PosVector();
        DirVector direction = new DirVector(1, 1, 0.2f);
        for (int i = 1; i < 15; i++) {
            position.add(direction.reducedTo(72, new DirVector()));
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
    protected Collection<EntityFactory> getInitialEntities() {
        List<EntityFactory> entities = new ArrayList<>();
        entities.add(new PowerupEntity.Factory(new PosVector(40, 40, 0), PowerupColor.GREEN));
        entities.add(new PowerupEntity.Factory(new PosVector(-100, 100, 100), PowerupColor.RED));
        entities.add(new PowerupEntity.Factory(new PosVector(-100, -50, 50), PowerupColor.YELLOW));
        entities.add(new PowerupEntity.Factory(new PosVector(0, 100, 20), PowerupColor.RED));
        entities.add(new PowerupEntity.Factory(new PosVector(0, -50, 150), PowerupColor.BLUE));
        return entities;

    }

    @Override
    public EntityState getNewSpawnPosition() {
        DirVector direction = new DirVector(1, 1, 0);
        direction.normalize();
        PosVector pos = new PosVector(nextSpawnPosition);

        nextSpawnPosition.add(new PosVector(20, 0, 0));
        return new EntityState(pos, direction, DirVector.zeroVector());
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.6f, 0.6f, 0.9f, 1f / FOG_DIST);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
