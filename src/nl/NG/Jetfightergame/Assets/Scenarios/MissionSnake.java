package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.GridMesh;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 2-3-2018.
 */
public class MissionSnake extends GameState {

    private static final float LEVEL_SQUARE_SIZE = 20f;
    private static final float LEVEL_HEIGHT = LEVEL_SQUARE_SIZE;
    private static final float PILLAR_BASE_SIZE = 5f;
    private static final float PILLAR_BASE_HEIGHT = PILLAR_BASE_SIZE;
    private static final float PILLAR_MIDDLE_SIZE = 2f;
    private static final float PILLAR_RING_SIZE = (PILLAR_BASE_SIZE + (2 * PILLAR_MIDDLE_SIZE)) / 3;

    private static final int LEVEL_SQUARE_DIM = 5;

    private static final PosVector VEC_GROUND = new PosVector(PILLAR_BASE_SIZE, PILLAR_BASE_SIZE, 0);
    private static final PosVector VEC_BOTTOM = new PosVector(PILLAR_RING_SIZE, PILLAR_RING_SIZE, PILLAR_BASE_HEIGHT / 2);
    private static final PosVector VEC_LOWER = new PosVector(PILLAR_MIDDLE_SIZE, PILLAR_MIDDLE_SIZE, PILLAR_BASE_HEIGHT);

    private static final Color4f WORLD_COLOR = Color4f.WHITE;
    private static final Material WORLD_MATERIAL = Material.ROUGH;

    private static final Shape gamePillar = makePillar();
    private static final Shape gameFloor = makeFloorTiles();

    public MissionSnake(GameTimer time) {
        super(time);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        final DirVector g = new DirVector(0, 0, -9.81f);
        return g.scale(entity.getMass(), g);
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }


    @Override
    protected Collection<Touchable> createWorld() {
        Collection<Touchable> staticEntities = new ArrayList<>();
        staticEntities.add(new StaticObject(gameFloor, WORLD_MATERIAL, Color4f.BLUE));

        for (int x = 0; x < (LEVEL_SQUARE_DIM + 1); x++) {
            for (int y = 0; y < (LEVEL_SQUARE_DIM + 1); y++) {
                final Vector offSet = new DirVector(LEVEL_SQUARE_SIZE * x, LEVEL_SQUARE_SIZE * y, 0);
                staticEntities.add(new StaticObject(gamePillar, WORLD_MATERIAL, WORLD_COLOR, offSet, null));
            }
        }

        final float offSet = LEVEL_SQUARE_SIZE * LEVEL_SQUARE_DIM;
        staticEntities.add(new StaticObject(gameFloor, WORLD_MATERIAL, Color4f.BLUE, new DirVector(offSet, 0, LEVEL_HEIGHT), new Vector3f(-1, 1, 1)));

        return staticEntities;
    }

    @Override
    protected Collection<MovingEntity> setEntities(SpawnReceiver deposit) {
        return Collections.EMPTY_SET;
    }

    /**
     * generates a pillar shape, with the predefined parameters
     */
    private static Shape makePillar(){
        CustomShape frame = new CustomShape();

        // create new vertices because we shouldn't mess with constants
        squareRing(frame, new PosVector(VEC_GROUND), new PosVector(VEC_BOTTOM));
        squareRing(frame, new PosVector(VEC_BOTTOM), new PosVector(VEC_LOWER));

        PosVector roof = new PosVector(VEC_GROUND);
        PosVector top = new PosVector(VEC_BOTTOM);
        PosVector upper = new PosVector(VEC_LOWER);
        roof.mul(1, 1, -1).add(0, 0, LEVEL_HEIGHT);
        top.mul(1, 1, -1).add(0, 0, LEVEL_HEIGHT);
        upper.mul(1, 1, -1).add(0, 0, LEVEL_HEIGHT);

        squareRing(frame, new PosVector(VEC_LOWER), upper);
        squareRing(frame, upper, top);
        squareRing(frame, top, roof);

        return frame.wrapUp(ServerSettings.RENDER_ENABLED);
    }

    /**
     * creates a ring/band by mirroring the given vectors in XZ and YZ planes.
     * @param frame the frame where these new quads be bestowed upon
     * @param lower the vector with the lowest z-value
     * @param upper the '' highest z-value
     */
    private static void squareRing(CustomShape frame, PosVector lower, PosVector upper) {
        final PosVector newMid = lower.middleTo(upper);
        newMid.mul(0, 0, 1);
        frame.setMiddle(newMid);

        frame.addQuad(lower, upper);
        frame.addQuad(lower.mirrorX(new PosVector()), upper.mirrorX(new PosVector()));
        frame.addMirrorQuad(lower, upper, upper.mirrorX(new PosVector()), lower.mirrorX(new PosVector()));
    }

    /**
     * generate a pattern of squares, implemented as a GridMesh for improved collision detection performance
     * @return a flat plane with tiling to allow pillars to be placed on every (x * SQUARE_SIZE, y * SQUARE_SIZE) coordinate
     */
    private static Shape makeFloorTiles(){
        final int nOfPillars = LEVEL_SQUARE_DIM + 1;
        PosVector[][] world = new PosVector[2 * nOfPillars][2 * nOfPillars];

        for (int x = 0; x < nOfPillars; x++) {
            for (int y = 0; y < nOfPillars; y++) {
                addPoint(world, x, y, false, false);
                addPoint(world, x, y, true, false);
                addPoint(world, x, y, false, true);
                addPoint(world, x, y, true, true);

            }
        }

        return new GridMesh(world);
    }

    private static void addPoint(PosVector[][] world, int x, int y, boolean positiveX, boolean positiveY) {
        final PosVector offset = new PosVector(VEC_GROUND);

        // not positive coordinate -> flip translation to negative
        if (!positiveX) offset.mirrorX(offset);
        if (!positiveY) offset.mirrorY(offset);

        offset.add(x * LEVEL_SQUARE_SIZE, y * LEVEL_SQUARE_SIZE, 0);

        final int xIndex = (2 * x) + (positiveX ? 1 : 0);
        final int yIndex = (2 * y) + (positiveY ? 1 : 0);
        world[xIndex][yIndex] = offset;
    }
}
