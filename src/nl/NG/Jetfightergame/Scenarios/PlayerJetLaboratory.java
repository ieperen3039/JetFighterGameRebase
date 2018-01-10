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
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class PlayerJetLaboratory extends GameState {
    public PlayerJetLaboratory(Controller input) {
        super(input);
    }

    @Override
    public void buildScene() {
        dynamicEntities.add(getPlayer());
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new ContainerCube(100));

        // for x = -1 and x = 1
        for (int x = -1; x < 2; x += 2) {
            // for y = -1 and y = 1
            for (int y = -1; y < 2; y += 2) {
                // etc.
                for (int z = -1; z < 2; z += 2) {
                    dynamicEntities.add(new FallingCube(
                            Material.SILVER, 1000, 20,
                            new PosVector(x, y, z),
                            new DirVector(), new Quaternionf(), getTimer().getRenderTime()
                    ));
                }
            }
        }

        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE));
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
