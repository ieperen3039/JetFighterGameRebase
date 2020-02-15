package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.StaticEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RacePathDescription;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen created on 13-2-2020.
 */
public class CubeWorld extends GameState {
    private static final int FOG_DIST = 10000;
    public static final int START_LINE_DIST = 100;

    private PosVector nextSpawnPosition = new PosVector(-1250, 0, 0);
    private DirVector nextSpawnOffset = new DirVector();

    private static RacePathDescription racePath = new RacePathDescription(Resource.CUBEMAP);

    @Override
    public EntityState getNewSpawnPosition() {
        PosVector pos = new PosVector(nextSpawnPosition);
        nextSpawnOffset.rotateAxis(1.396f, -1, 0, 0); // allows up to 9 players
        pos.add(nextSpawnOffset);

        Quaternionf rotation = new Quaternionf();
        return new EntityState(pos, rotation, DirVector.zeroVector());
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

    @Override
    public Color4f fogColor() {
        return new Color4f(0.3f, 0.3f, 0.4f, 1f / FOG_DIST);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    protected Collection<Touchable> createWorld(
            RaceProgress raceProgress, GameTimer timer
    ) {
        List<Touchable> entities = new ArrayList<>();

        // land
        for (Shape s : GeneralShapes.CUBE_ISLAND) {
            entities.add(new StaticEntity(s, Material.SILVER, Color4f.WHITE));
        }

        Pair<PosVector, DirVector> start = racePath.getFirstCheckpoint();
        nextSpawnPosition.set(start.left);
        DirVector offset = DirVector.yVector();
        DirVector backwait = new DirVector(start.right).scale(-START_LINE_DIST);
        offset.cross(start.right).normalize(30).add(backwait);
        nextSpawnOffset = offset;

        for (RaceProgress.Checkpoint ch : racePath.getCheckpoints(raceProgress, Color4f.RED)) {
            entities.add(ch);
        }

        return entities;
    }
}
