package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Player;
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

    private static final int CUBE_SIZE = 100;

    public PlayerJetLaboratory(GameTimer time) {
        super(time);
    }

    @Override
    public void buildScene(Player player) {
        player.jet().set(PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf());
        dynamicEntities.add(player.jet());
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new ContainerCube(CUBE_SIZE));

        // for x = -1 and x = 1
        for (int x = -1; x < 2; x += 2) {
            // for y = -1 and y = 1
            for (int y = -1; y < 2; y += 2) {
                // etc.
                for (int z = -1; z < 2; z += 2) {
                    dynamicEntities.add(new FallingCube(
                            Material.SILVER, 1000, 20,
                            new PosVector((x * CUBE_SIZE) / 2, (y * CUBE_SIZE) / 2, (z * CUBE_SIZE) / 2),
                            new DirVector(), new Quaternionf(), getTimer()
                    ));
                }
            }
        }

        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE.darken(0.7f)));
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
