package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;

import static nl.NG.Jetfightergame.ServerNetwork.EntityClass.FALLING_CUBE_SMALL;

/**
 * @author Geert van Ieperen
 * created on 7-1-2018.
 */
public class PlayerJetLaboratory extends GameState {

    private static final int LAB_SIZE = 200;

    @Override
    protected Collection<Touchable> createWorld() {
        ArrayList<Touchable> entities = new ArrayList<>();
        int ind = 0;

        for (Shape shape : GeneralShapes.ISLAND1) {
            entities.add(new StaticObject(
                    shape, Material.ROUGH, getColor(ind++), new PosVector(0, 0, -500)
            ));
        }

//        entities.add(new StaticObject(
//                GeneralShapes.makeInverseCube(3, ServerSettings.RENDER_ENABLED),
//                Material.PLASTIC, Color4f.ORANGE, LAB_SIZE
//        ));

        return entities;
    }

    private Color4f getColor(int ind) {
        switch (ind % 2) {
            case 0:
                return Color4f.BLUE;
            case 1:
                return Color4f.RED;
        }
        return Color4f.INVISIBLE;

//        return new Color4f((ind * 36) % 255, (ind * 52) % 255, (ind * 11) % 255, 1);
    }

    @Override
    protected Collection<Spawn> getInitialEntities() {
        Collection<Spawn> dynamicEntities = new ArrayList<>();

        int[] vals = new int[]{-1, 1};
        // for x = -1 and x = 1
        for (int x : vals) {
            // for y = -1 and y = 1
            for (int y : vals) {
                // etc.
//                for (int z : vals) {
                {
                    int z = 2;
                    dynamicEntities.add(new Spawn(EntityClass.FALLING_CUBE_LARGE,
                            new PosVector((x * LAB_SIZE) / 2, (y * LAB_SIZE) / 2, (z * LAB_SIZE) / 2),
                            new Quaternionf(), new DirVector()
                    ));
                }
            }
        }

        dynamicEntities.add(new Spawn(FALLING_CUBE_SMALL,
                new PosVector(20, 0, 0), new Quaternionf(), new DirVector(0, 0, -50)
        ));

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
