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
import nl.NG.Jetfightergame.GameState.RacePathDescription;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.GameState.RaceProgress.Checkpoint;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class IslandMap extends GameState {
    private static final int FOG_DIST = 750;
    private PosVector nextSpawnPosition = new PosVector();
    private DirVector nextSpawnOffset = new DirVector(0, 30, 0);

    private static RacePathDescription racePath = new RacePathDescription(Resource.GLITCHMAP);

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        List<Touchable> entities = new ArrayList<>();

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.GLASS, Color4f.BLACK));
        }

        nextSpawnPosition = new PosVector(-50, 400, 0);

        for (Checkpoint ch : racePath.getCheckpoints(raceProgress, Color4f.BLUE)) {
            entities.add(ch);
        }

        return entities;
    }

    private Checkpoint makeCheckpoint(RaceProgress raceProgress, int x, int y, int z, float dx, float dy, float dz, int radius) {
        return raceProgress.addCheckpoint(new PosVector(x, y, z), new DirVector(dx, dy, dz), radius, Color4f.BLUE);
    }

    private Checkpoint makeRoadPoint(RaceProgress raceProgress, int x, int y, int z, float dx, float dy, float dz) {
        return raceProgress.addRoadpoint(new PosVector(x, y, z), new DirVector(dx, dy, dz), 500);
    }

    @Override
    protected Collection<EntityFactory> getInitialEntities() {
        List<EntityFactory> entities = new ArrayList<>();

        // powerups
        for (PowerupEntity.Factory pop : racePath.getPowerups()) {
            entities.add(pop);
        }

        return entities;

    }

    private PowerupEntity.Factory powerup(int x, int y, int z, PowerupColor green) {
        return new PowerupEntity.Factory(new PosVector(x, y, z), green);
    }

    @Override
    public EntityState getNewSpawnPosition() {
        PosVector pos = new PosVector(nextSpawnPosition);
        nextSpawnOffset.rotateAxis(1.396f, -1, 0, 0); // allows up to 9 players
        pos.add(nextSpawnOffset);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        return new EntityState(pos, rotation, DirVector.zeroVector());
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.7f, 0.7f, 0.8f, 1f / FOG_DIST);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
