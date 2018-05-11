package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ScreenOverlay.HUD.HUDTargetable;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class PlayerJetLaboratory extends GameState {

    private static final int LAB_SIZE = 300;
    private Collection<HUDTargetable> cubeTargets = new ArrayList<>();

    public PlayerJetLaboratory(GameTimer time) {
        super(time);
    }

    @Override
    protected Collection<Touchable> createWorld() {
        return Collections.singleton(new StaticObject(GeneralShapes.makeInverseCube(0, ServerSettings.RENDER_ENABLED), Material.ROUGH, Color4f.ORANGE, LAB_SIZE));
    }

    @Override
    protected Collection<MovingEntity> setEntities() {
        Collection<MovingEntity> dynamicEntities = new ArrayList<>();

        // for x = -1 and x = 1
        for (int x = -1; x < 2; x += 2) {
            // for y = -1 and y = 1
            for (int y = -1; y < 2; y += 2) {
                // etc.
                for (int z = -1; z < 2; z += 2) {
                    final FallingCube cube = new FallingCube(
                            Identity.next(), Material.SILVER, 500, LAB_SIZE/10,
                            new PosVector((x * LAB_SIZE) / 2, (y * LAB_SIZE) / 2, (z * LAB_SIZE) / 2),
                            new DirVector(), new Quaternionf(), getTimer(), this
                    );

                    dynamicEntities.add(cube);
                    cubeTargets.add(getHUDTarget(cube));
                }
            }
        }
        return dynamicEntities;
    }



    @Override
    public void cleanUp() {
        super.cleanUp();
        cubeTargets.forEach(HUDTargetable::dispose);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }
}
