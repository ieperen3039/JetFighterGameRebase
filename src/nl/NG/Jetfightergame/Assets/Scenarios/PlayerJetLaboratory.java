package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Prentity;
import nl.NG.Jetfightergame.AbstractEntities.StaticEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Entities.FallingCube;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class PlayerJetLaboratory extends GameState {

    private static final int LAB_SIZE = 200;

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        ArrayList<Touchable> entities = new ArrayList<>();

        entities.add(new StaticEntity(
                GeneralShapes.LAB_CUBE,
                Material.CYBERGLASSMETAL, Color4f.BLACK, PosVector.zeroVector(), LAB_SIZE
        ));

        return entities;
    }

    @Override
    protected Collection<Prentity> getInitialEntities() {
        Collection<Prentity> dynamicEntities = new ArrayList<>();

        int[] vals = new int[]{-1, 1};
        // for x = -1 and x = 1
        for (int x : vals) {
            // for y = -1 and y = 1
            for (int y : vals) {
                // etc.
                for (int z : vals) {
                    dynamicEntities.add(new Prentity(FallingCube.LARGE,
                            new PosVector((x * LAB_SIZE) / 2, (y * LAB_SIZE) / 2, (z * LAB_SIZE) / 2),
                            new Quaternionf(), new DirVector()
                    ));
                }
            }
        }

        return dynamicEntities;
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }

    @Override
    public MovingEntity.State getNewSpawnPosition() {
        return new MovingEntity.State();
    }
}
