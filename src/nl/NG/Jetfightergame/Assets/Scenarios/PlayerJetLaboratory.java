package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Assets.WorldObjects.Tunnel;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.Settings.ServerSettings;
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

    private static final int LAB_SIZE = 300;

    public PlayerJetLaboratory(GameTimer time) {
        super(time);
    }

    @Override
    protected Collection<Touchable> createWorld() {
        ArrayList<Touchable> entities = new ArrayList<>();

        PosVector A = new PosVector(-100, 0, 0);
        PosVector B = new PosVector(-100, 50, 0);
        PosVector C = new PosVector(0, 100, 50);
        PosVector D = new PosVector(50, 100, 50);

        entities.add(new Tunnel(
                A, B, C, D, 20, 10, 16, ServerSettings.RENDER_ENABLED
        ));
        entities.add(new StaticObject(
                GeneralShapes.makeInverseCube(0, ServerSettings.RENDER_ENABLED),
                Material.ROUGH, Color4f.ORANGE, LAB_SIZE
        ));
        return entities;
    }

    @Override
    protected Collection<MovingEntity> setEntities(SpawnReceiver deposit) {
        Collection<MovingEntity> dynamicEntities = new ArrayList<>();

        // for x = -1 and x = 1
        for (int x = -1; x < 2; x += 2) {
            // for y = -1 and y = 1
            for (int y = -1; y < 2; y += 2) {
                // etc.
                for (int z = -1; z < 2; z += 2) {
                    MovingEntity cube = EntityClass.FALLING_CUBE_LARGE.construct(
                            Identity.next(), deposit, null,
                            new PosVector((x * LAB_SIZE) / 2, (y * LAB_SIZE) / 2, (z * LAB_SIZE) / 2),
                            new Quaternionf(), new DirVector()
                    );

                    dynamicEntities.add(cube);
                }
            }
        }

        return dynamicEntities;
    }



    @Override
    public void cleanUp() {
        super.cleanUp();
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
