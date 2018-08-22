package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.StaticEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Geert van Ieperen. Created on 5-7-2018.
 */
public class IslandMap extends GameState {
    private static final int FOG_DIST = 750;
    private PosVector nextSpawnPosition = new PosVector();
    private DirVector nextSpawnOffset = new DirVector(0, 30, 0);

    @Override
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        List<Touchable> entities = new ArrayList<>();

        for (Shape s : GeneralShapes.ISLAND1) {
            entities.add(new StaticEntity(s, Material.GLASS, Color4f.BLACK, new PosVector(0, 0, -250)));
        }

        nextSpawnPosition = new PosVector(-50, 400, 0);
//        entities.add(makeCheckpoint(raceProgress, -150, 400, -90, -1.00f, 0.00f, 0.00f, 100));
//        entities.add(makeRoadPoint(raceProgress, -188, 400, -90, -1.00f, 0.00f, 0.00f));
//        entities.add(makeRoadPoint(raceProgress, -236, 400, -90, -1.00f, 0.00f, 0.00f));
//        entities.add(makeRoadPoint(raceProgress, -284, 399, -90, -0.98f, -0.19f, 0.00f));
//        entities.add(makeRoadPoint(raceProgress, -333, 393, -90, -0.82f, -0.58f, 0.00f));
//        entities.add(makeCheckpoint(raceProgress, -375, 372, -90, -0.5f, -0.5f, -0.02f, 100));
//        entities.add(makeRoadPoint(raceProgress, -403, 338, -92, -0.11f, -0.99f, -0.10f));
//        entities.add(makeRoadPoint(raceProgress, -419, 296, -96, -0.01f, -0.99f, -0.10f));
//        entities.add(makeRoadPoint(raceProgress, -426, 250, -101, -0.01f, -1.00f, -0.09f));
//        entities.add(makeRoadPoint(raceProgress, -429, 201, -106, -0.01f, -0.99f, -0.10f));
//        entities.add(makeRoadPoint(raceProgress, -431, 152, -111, -0.02f, -0.99f, -0.12f));
//        entities.add(makeCheckpoint(raceProgress, -432, 102, -116, -0.02f, -0.99f, -0.12f, 100));
//        entities.add(makeRoadPoint(raceProgress, -433, 53, -122, -0.02f, -0.99f, -0.12f));
//        entities.add(makeRoadPoint(raceProgress, -434, 3, -128, -0.01f, -0.99f, -0.10f));
//        entities.add(makeRoadPoint(raceProgress, -435, -46, -133, -0.01f, -1.00f, -0.04f));
//        entities.add(makeCheckpoint(raceProgress, -435, -96, -135, 0.00f, -1.00f, 0.00f, 75));
//        entities.add(makeRoadPoint(raceProgress, -435, -146, -135, -0.01f, -1.00f, -0.08f));
//        entities.add(makeRoadPoint(raceProgress, -435, -196, -140, 0.10f, -0.96f, -0.25f));
//        entities.add(makeRoadPoint(raceProgress, -433, -244, -151, 0.16f, -0.93f, -0.34f));
//        entities.add(makeRoadPoint(raceProgress, -427, -290, -167, 0.15f, -0.91f, -0.39f));
//        entities.add(makeRoadPoint(raceProgress, -422, -325, -181, 0.15f, -0.90f, -0.40f));
//        entities.add(makeRoadPoint(raceProgress, -417, -363, -198, 0.15f, -0.90f, -0.41f));
//        entities.add(makeRoadPoint(raceProgress, -410, -406, -217, 0.15f, -0.90f, -0.41f));
//        entities.add(makeRoadPoint(raceProgress, -402, -449, -237, 0.48f, -0.78f, -0.40f));
//        entities.add(makeRoadPoint(raceProgress, -384, -490, -255, 0.74f, -0.56f, -0.38f));
//        entities.add(makeRoadPoint(raceProgress, -354, -523, -271, 0.87f, -0.35f, -0.33f));
//        entities.add(makeRoadPoint(raceProgress, -315, -545, -284, 0.95f, -0.13f, -0.27f));
//        entities.add(makeCheckpoint(raceProgress, -270, -557, -295, 0.98f, -0.01f, -0.19f, 200));
//        entities.add(makeRoadPoint(raceProgress, -222, -561, -303, 0.99f, 0.04f, -0.15f));
//        entities.add(makeRoadPoint(raceProgress, -173, -562, -310, 0.98f, 0.03f, -0.17f));
//        entities.add(makeRoadPoint(raceProgress, -124, -561, -318, 0.99f, 0.04f, -0.16f));
//        entities.add(makeRoadPoint(raceProgress, -75, -559, -325, 0.99f, 0.09f, -0.07f));
//        entities.add(makeRoadPoint(raceProgress, -25, -554, -329, 0.98f, 0.19f, 0.07f));
//        entities.add(makeRoadPoint(raceProgress, 23, -545, -326, 0.96f, 0.24f, 0.14f));
//        entities.add(makeRoadPoint(raceProgress, 71, -534, -320, 0.96f, 0.25f, 0.15f));
//        entities.add(makeRoadPoint(raceProgress, 119, -522, -312, 0.95f, 0.26f, 0.19f));
//        entities.add(makeRoadPoint(raceProgress, 166, -509, -303, 0.94f, 0.21f, 0.26f));
//        entities.add(makeRoadPoint(raceProgress, 213, -497, -292, 0.93f, 0.27f, 0.23f));
//        entities.add(makeRoadPoint(raceProgress, 260, -484, -280, 0.92f, 0.34f, 0.19f));
//        entities.add(makeRoadPoint(raceProgress, 306, -468, -269, 0.87f, 0.48f, 0.13f));
//        entities.add(makeRoadPoint(raceProgress, 359, -445, -260, 0.75f, 0.66f, 0.04f));
//        entities.add(makeRoadPoint(raceProgress, 407, -415, -256, 0.68f, 0.73f, -0.02f));
//        entities.add(makeCheckpoint(raceProgress, 453, -381, -255, 0.63f, 0.77f, -0.04f, 200));
//        entities.add(makeRoadPoint(raceProgress, 475, -343, -254, 0.57f, 0.82f, -0.08f));
//        entities.add(makeRoadPoint(raceProgress, 494, -303, -253, 0.53f, 0.85f, 0.03f));
//        entities.add(makeRoadPoint(raceProgress, 510, -261, -253, 0.47f, 0.88f, 0.09f));
//        entities.add(makeRoadPoint(raceProgress, 533, -217, -249, 0.28f, 0.94f, 0.18f));
//        entities.add(makeRoadPoint(raceProgress, 547, -171, -241, 0.15f, 0.94f, 0.32f));
//        entities.add(makeRoadPoint(raceProgress, 555, -124, -229, 0.01f, 0.96f, 0.29f));
//        entities.add(makeRoadPoint(raceProgress, 556, -76, -215, -0.08f, 0.96f, 0.28f));
//        entities.add(makeRoadPoint(raceProgress, 552, -29, -201, -0.20f, 0.93f, 0.30f));
//        entities.add(makeRoadPoint(raceProgress, 543, 18, -185, -0.28f, 0.90f, 0.35f));
//        entities.add(makeRoadPoint(raceProgress, 530, 62, -169, -0.34f, 0.86f, 0.37f));
//        entities.add(makeRoadPoint(raceProgress, 513, 106, -151, -0.42f, 0.82f, 0.38f));
//        entities.add(makeRoadPoint(raceProgress, 492, 147, -133, -0.45f, 0.81f, 0.38f));
//        entities.add(makeRoadPoint(raceProgress, 480, 192, -114, -0.53f, 0.76f, 0.38f));
//        entities.add(makeRoadPoint(raceProgress, 480, 236, -95, -0.66f, 0.70f, 0.27f));
//        entities.add(makeCheckpoint(raceProgress, 480, 300, -80, -0.80f, 0.58f, 0.12f, 200));
//        entities.add(makeRoadPoint(raceProgress, 420, 310, -71, -0.90f, 0.44f, -0.02f));
//        entities.add(makeRoadPoint(raceProgress, 351, 312, -67, -0.93f, 0.37f, -0.03f));
//        entities.add(makeRoadPoint(raceProgress, 216, 332, -66, -0.94f, 0.35f, -0.02f));
//        entities.add(makeRoadPoint(raceProgress, 239, 351, -67, -0.94f, 0.34f, -0.02f));
//        entities.add(makeRoadPoint(raceProgress, 193, 369, -68, -0.96f, 0.27f, -0.08f));
//        entities.add(makeCheckpoint(raceProgress, 145, 384, -71, -0.97f, 0.23f, -0.09f, 100));
//        entities.add(makeRoadPoint(raceProgress, 97, 397, -76, -0.99f, 0.15f, -0.08f));
//        entities.add(makeRoadPoint(raceProgress, 49, 400, -80, -0.99f, 0.04f, -0.14f));
//        entities.add(makeRoadPoint(raceProgress, 0, 400, -85, -0.98f, 0.05f, -0.20f));
//        entities.add(makeRoadPoint(raceProgress, -50, 400, -90, -1.00f, 0.00f, 0.00f));
//        entities.add(makeRoadPoint(raceProgress, -100, 400, -90, -1.00f, 0.00f, 0.00f));

        return entities;
    }

    private RaceProgress.Checkpoint makeCheckpoint(RaceProgress raceProgress, int x, int y, int z, float dx, float dy, float dz, int radius) {
        return raceProgress.addCheckpoint(new PosVector(x, y, z), new DirVector(dx, dy, dz), radius, Color4f.BLUE);
    }

    private RaceProgress.Checkpoint makeRoadPoint(RaceProgress raceProgress, int x, int y, int z, float dx, float dy, float dz) {
        return raceProgress.addRoadpoint(new PosVector(x, y, z), new DirVector(dx, dy, dz), 500);
    }

    @Override
    protected Collection<EntityFactory> getInitialEntities() {
        List<EntityFactory> entities = new ArrayList<>();

        entities.add(powerup(-399, 352, -89, PowerupColor.RED));
        entities.add(powerup(-381, 294, -122, PowerupColor.GREEN));
        entities.add(powerup(-405, 105, -128, PowerupColor.YELLOW));
        entities.add(powerup(-424, 3, -129, PowerupColor.GREEN));
        entities.add(powerup(-332, -528, -269, PowerupColor.BLUE));
        entities.add(powerup(175, -502, -275, PowerupColor.GREEN));
        entities.add(powerup(393, -438, -272, PowerupColor.BLUE));
        entities.add(powerup(500, -38, -195, PowerupColor.YELLOW));
        entities.add(powerup(512, 51, -196, PowerupColor.GREEN));
        entities.add(powerup(430, 217, -137, PowerupColor.RED));
        entities.add(powerup(380, 282, -83, PowerupColor.BLUE));
        entities.add(powerup(295, 329, -67, PowerupColor.GREEN));

        entities.add(powerup(-485, 6, -144, PowerupColor.RED));
        entities.add(powerup(-455, -111, -142, PowerupColor.YELLOW));
        entities.add(powerup(-402, -460, -179, PowerupColor.GREEN));
        entities.add(powerup(-456, -292, -174, PowerupColor.BLUE));
        entities.add(powerup(-424, -379, -209, PowerupColor.RED));
        entities.add(powerup(233, -486, -356, PowerupColor.YELLOW));
        entities.add(powerup(319, -495, -290, PowerupColor.GREEN));
        entities.add(powerup(514, -314, -128, PowerupColor.RED));
        entities.add(powerup(570, -208, -137, PowerupColor.BLUE));
        entities.add(powerup(552, 186, -92, PowerupColor.YELLOW));
        entities.add(powerup(491, 176, -81, PowerupColor.GREEN));
        entities.add(powerup(276, 418, 2, PowerupColor.RED));
        entities.add(powerup(51, 428, -83, PowerupColor.RED));
        entities.add(powerup(-32, 492, -115, PowerupColor.GREEN));
        entities.add(powerup(-92, 536, -90, PowerupColor.BLUE));
        entities.add(powerup(-323, 395, -55, PowerupColor.YELLOW));

        return entities;

    }

    private PowerupEntity.Factory powerup(int x, int y, int z, PowerupColor green) {
        return new PowerupEntity.Factory(new PosVector(x, y, z), green);
    }

    @Override
    public EntityState getNewSpawnPosition() {
        PosVector pos = new PosVector(nextSpawnPosition);
        nextSpawnOffset.rotateAxis(1.396f, -1, 0, 0);
        pos.add(nextSpawnOffset);

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        return new EntityState(pos, rotation, DirVector.zeroVector());
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.7f, 0.7f, 0.8f, 1f / FOG_DIST);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
