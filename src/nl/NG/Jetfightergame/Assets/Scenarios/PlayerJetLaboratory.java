package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.GeneralEntities.ContainerCube;
import nl.NG.Jetfightergame.Assets.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Engine.GameState.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.Tools.Pair;
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

    private static final int LAB_SIZE = 100;
    private Collection<HUDTargetable> cubeTargets = new ArrayList<>();

    public PlayerJetLaboratory(GameTimer time, Player player) {
        super(player, time);

        player.jet().set(PosVector.zeroVector(), DirVector.zeroVector(), new Quaternionf());
        dynamicEntities.add(player.jet());
//        staticEntities.add(new SimplexCave());
        staticEntities.add(new ContainerCube(LAB_SIZE));

        // for x = -1 and x = 1
        for (int x = -1; x < 2; x += 2) {
            // for y = -1 and y = 1
            for (int y = -1; y < 2; y += 2) {
                // etc.
                for (int z = -1; z < 2; z += 2) {
                    final FallingCube cube = new FallingCube(
                            Material.SILVER, 500, 10,
                            new PosVector((x * LAB_SIZE) / 2, (y * LAB_SIZE) / 2, (z * LAB_SIZE) / 2),
                            new DirVector(), new Quaternionf(), getTimer()
                    );

                    dynamicEntities.add(cube);
                    cubeTargets.add(getHUDTarget(cube));
                }
            }
        }

        lights.add(new Pair<>(new PosVector(4, 3, 6), Color4f.WHITE.darken(0.7f)));
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
        cubeTargets.forEach(HUDTargetable::dispose);
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }
}
