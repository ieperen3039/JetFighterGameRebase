package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 26-12-2017.
 */
public class CollisionLaboratory extends GameState {

    private static final float CUBESIZE = 1f;
    private static final float CUBEMASS = 2f;
    private static final int LAB_SIZE = 10;
    private static final int NR_OF_CUBES = 2;
    private static final float INIT_SPEED = LAB_SIZE/5f;

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
        this.speeds = INIT_SPEED;
//        this.speeds = labSize/3;
//        getTimer().setGameTimeMultiplier(2f);
    }

    @Override
    public void buildScene(Player player) {
        Settings.SPECTATOR_MODE = true;
        player.jet().set();

        int gridSize = (int) Math.ceil(Math.cbrt(nrOfCubes));
        int interSpace = (2 * labSize) / (gridSize + 1);

        staticEntities.add(new ContainerCube(labSize));

        int remainingCubes = this.nrOfCubes;

        cubing:
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int z = 0; z < gridSize; z++) {
                    final PosVector pos = new PosVector(-labSize + ((x + 1) * interSpace), -labSize + ((y + 1) * interSpace), -labSize + ((z + 1) * interSpace));
                    DirVector random = Vector.random();
                    random.scale(speeds, random);
                    FallingCube cube = new FallingCube(
                            Material.SILVER, CUBEMASS, CUBESIZE,
                            pos.scale(0.8f, pos).toPosVector(),
                            random, new Quaternionf().rotate(2, 1, 1), getTimer()
                    );
                    cube.addRandomRotation(0.2f);
                    dynamicEntities.add(cube);
                    if (--remainingCubes <= 0) break cubing;
                }
            }
        }

        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.TRANSPARENT_GREY));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
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

}
