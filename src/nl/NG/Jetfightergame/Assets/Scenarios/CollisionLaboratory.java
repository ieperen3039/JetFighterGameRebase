package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
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

    public CollisionLaboratory(GameTimer time) {
        this(LAB_SIZE, NR_OF_CUBES, time);
    }

    public CollisionLaboratory(int labSize, int nrOfCubes, GameTimer time) {
        super(time);
        this.labSize = labSize;
        this.nrOfCubes = nrOfCubes;
        this.speeds = labSize / 3f;

        ClientSettings.SPECTATOR_MODE = true;
    }

    @Override
    protected Collection<Touchable> createWorld() {
//        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.WHITE.darken(0.3f)));
        return Collections.singletonList(new StaticObject(GeneralShapes.INVERSE_CUBE, null, Material.ROUGH, Color4f.ORANGE, labSize));
    }

    @Override
    protected Collection<MovingEntity> setEntities(SpawnReceiver deposit) {
        int gridSize = (int) Math.ceil(Math.cbrt(nrOfCubes));
        int interSpace = (2 * labSize) / (gridSize + 1);

        int remainingCubes = nrOfCubes;

        Collection<MovingEntity> cubes = new ArrayList<>(nrOfCubes);

        cubing:
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int z = 0; z < gridSize; z++) {
                    cubes.add(makeCube(labSize, speeds, interSpace, x, y, z, deposit));

                    if (--remainingCubes <= 0) break cubing;
                }
            }
        }

        return cubes;
    }

    private MovingEntity makeCube(int labSize, float speeds, int interSpace, int x, int y, int z, SpawnReceiver deposit) {
        final PosVector pos = new PosVector(
                -labSize + ((x + 1) * interSpace), -labSize + ((y + 1) * interSpace), -labSize + ((z + 1) * interSpace)
        );

        DirVector random = Vector.random();
        random.scale(speeds, random);

        return EntityClass.FALLING_CUBE_SMALL.construct(
                Identity.next(), deposit, null,
                pos, new Quaternionf(), random
        );
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
}
