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
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.PI;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class IslandMap extends GameState {
    private static final int FOG_DIST = 1000;
    public static final int CHPOINTS = 10;
    private PosVector nextSpawnPosition = PosVector.zeroVector();

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        List<Touchable> entities = new ArrayList<>();

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.ROUGH, new Color4f(0.2f, 0.4f, 0.2f), new PosVector(0, 0, -250)));
        }

        TrackedVector<PosVector> position = new TrackedVector<>(new PosVector(10, 0, 0));
        DirVector addition = new DirVector(20, 0, 0);
        for (int i = 1; i < CHPOINTS; i++) {
            position.addUpdate(addition, new PosVector());
            PosVector pos = position.current();
            DirVector dir = position.difference();
            entities.add(raceProgress.addCheckpoint(pos, dir, 20, Color4f.BLUE));
            addition.rotateZ((float) ((3 * PI) / CHPOINTS));
        }

        return entities;
    }

    @Override
    protected Collection<EntityFactory> getInitialEntities() {
        List<EntityFactory> entities = new ArrayList<>();
        entities.add(new PowerupEntity.Factory(new PosVector(-40, 20, 0), PowerupColor.GREEN));
        entities.add(new PowerupEntity.Factory(new PosVector(-100, 100, 100), PowerupColor.RED));
        entities.add(new PowerupEntity.Factory(new PosVector(-100, -50, 50), PowerupColor.YELLOW));
        entities.add(new PowerupEntity.Factory(new PosVector(0, 100, 20), PowerupColor.RED));
        entities.add(new PowerupEntity.Factory(new PosVector(0, -50, 150), PowerupColor.BLUE));
        return entities;

    }

    @Override
    public EntityState getNewSpawnPosition() {
        DirVector direction = DirVector.xVector();
        PosVector pos = new PosVector(nextSpawnPosition);

        nextSpawnPosition.add(new PosVector(0, 20, 0));
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
