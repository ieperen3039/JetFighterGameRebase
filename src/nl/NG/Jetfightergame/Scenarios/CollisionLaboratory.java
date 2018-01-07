package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.GeneralEntities.FallingCube;
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

    private static final float CUBESIZE = 2f;
    private static final float CUBEMASS = 5f;
    private static final int LAB_SIZE = 20;
    private static final int NR_OF_CUBES = 2;
    private static final int INIT_SPEED = 5;


    private final int labSize;
    private final int nrOfCubes;
    private final int speeds;

    public CollisionLaboratory(Controller input) {
        this(input, LAB_SIZE, NR_OF_CUBES);
    }

    public CollisionLaboratory(Controller controller, int labSize, int nrOfCubes) {
        super(controller);
        this.labSize = labSize;
        this.nrOfCubes = nrOfCubes;
        this.speeds = INIT_SPEED;
//        this.speeds = labSize/3;
    }

    @Override
    protected void buildScene() {
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
                    dynamicEntities.add(new FallingCube(
                            Material.SILVER, CUBEMASS, CUBESIZE,
                            pos.scale(0.8f, pos).toPosVector(),
                            random, new Quaternionf(), time.getRenderTime()
                    ));
                    if (--remainingCubes <= 0) break cubing;
                }
            }
        }

        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.TRANSPARENT_GREY));
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
//        final DirVector random = DirVector.zeroVector();
//        return random.scale(labSize, random);
        return entity.getPosition().to(Vector.random(), new DirVector());
    }

}
