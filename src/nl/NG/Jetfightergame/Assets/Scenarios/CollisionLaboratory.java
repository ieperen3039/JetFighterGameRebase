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
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 26-12-2017.
 */
public class CollisionLaboratory extends GameState {

    private static final float CUBESIZE = 1f;
    private static final float CUBEMASS = 10f;
    private static final int LAB_SIZE = 10;
    private static final int NR_OF_CUBES = 4*4*4;

    private final int labSize;
    private final int nrOfCubes;
    private final float speeds;

    public CollisionLaboratory() {
        this(LAB_SIZE, NR_OF_CUBES);
    }

    public CollisionLaboratory(int labSize, int nrOfCubes) {
        super();
        this.labSize = labSize;
        this.nrOfCubes = nrOfCubes;
        this.speeds = labSize / 3f;

        ClientSettings.SPECTATOR_MODE = true;
    }

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
//        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.WHITE.darken(0.3f)));
        return Collections.singletonList(new StaticEntity(GeneralShapes.INVERSE_CUBE, Material.ROUGH, Color4f.ORANGE, null, labSize));
    }

    @Override
    protected Collection<Prentity> getInitialEntities() {
        int gridSize = (int) Math.ceil(Math.cbrt(nrOfCubes));
        int interSpace = (2 * labSize) / (gridSize + 1);

        int remainingCubes = nrOfCubes;

        Collection<Prentity> cubes = new ArrayList<>(nrOfCubes);

        cubing:
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int z = 0; z < gridSize; z++) {
                    cubes.add(makeCube(labSize, speeds, interSpace, x, y, z));

                    if (--remainingCubes <= 0) break cubing;
                }
            }
        }

        return cubes;
    }

    private Prentity makeCube(int labSize, float speeds, int interSpace, int x, int y, int z) {
        final PosVector pos = new PosVector(
                -labSize + ((x + 1) * interSpace), -labSize + ((y + 1) * interSpace), -labSize + ((z + 1) * interSpace)
        );

        DirVector random = Vector.random();
        random.scale(speeds, random);

        return new Prentity(FallingCube.SMALL, pos, new Quaternionf(), random);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public DirVector entityNetforce(MovingEntity entity) {

        final int version = 3;

        switch (version){
            case 1:
                // toward middle
                final DirVector middle = DirVector.random();//.scale(labSize, new DirVector());
                entity.getPosition().to(middle, middle);
                return middle;
            case 2:
                // random
                return DirVector.random().scale(labSize, new DirVector());
            case 3:
                // gravity
                final DirVector g = new DirVector(0, 0, -9.81f);
                return g.scale(entity.getMass(), g);
        }
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
