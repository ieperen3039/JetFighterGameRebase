package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.AbstractEntities.FallingCube;
import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
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

    private static final float CUBESIZE = 8f;
    private static final float CUBEMASS = 5f;
    private final int labSize;
    private final int nrOfCubes;
    private final int speeds;

    public CollisionLaboratory(int labSize, int nrOfCubes, Controller controller) {
        super(controller);
        this.labSize = labSize;
        this.nrOfCubes = nrOfCubes;
        this.speeds = labSize/5;
    }

    @Override
    protected void buildScene() {
        int gridSize = (int) Math.ceil(Math.cbrt(nrOfCubes));
        int interSpace = (2 * labSize) / (gridSize + 1);

        staticEntities.add(new TestLab(labSize));

        int remainingCubes = this.nrOfCubes;
        for (int x = 0; remainingCubes > 0; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int z = 0; z < gridSize; z++) {
                    dynamicEntities.add(new FallingCube(
                            Material.SILVER, CUBEMASS, CUBESIZE,
                            new PosVector(-labSize + (x + 1) * interSpace, -labSize + (y + 1) * interSpace, -labSize + (z + 1) * interSpace),
                            Vector.random().scale(speeds, new DirVector()), new Quaternionf(), time.getRenderTime()
                    ));
                    remainingCubes--;
                }
            }
        }

        lights.add(new Pair<>(PosVector.zeroVector(), Color4f.WHITE));
    }

    @Override
    protected DirVector entityNetforce(GameEntity entity) {
        final DirVector dest = new DirVector();
        entity.getPosition().to(Vector.random().scale(labSize, dest), dest);
        return dest;
    }
}
