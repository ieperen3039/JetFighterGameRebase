package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.FallingCube;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Shaders.Material;
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
    private final int labSize;
    private final int nrOfCubes;
    private final int speeds;

    public CollisionLaboratory(Controller input) {
        this(20, 1, input);
    }

    public CollisionLaboratory(int labSize, int nrOfCubes, Controller controller) {
        super(controller);
        this.labSize = labSize;
        this.nrOfCubes = nrOfCubes;
        this.speeds = labSize/3;

//        time.setGameTimeMultiplier(0.1f);
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
                    final PosVector pos = new PosVector(-labSize + (x + 1) * interSpace, -labSize + (y + 1) * interSpace, -labSize + (z + 1) * interSpace);
                    dynamicEntities.add(new FallingCube(
                            Material.SILVER, CUBEMASS, CUBESIZE,
                            pos.scale(0.8f, pos).toPosVector(),
                            Vector.random().scale(speeds, new DirVector()), new Quaternionf(), time.getRenderTime()
                    ));
                    if (--remainingCubes <= 0) break cubing;
                }
            }
        }

        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.WHITE));
    }

    @Override
    protected DirVector entityNetforce(GameEntity entity) {
        return DirVector.zVector().negate(new DirVector());
    }
}
