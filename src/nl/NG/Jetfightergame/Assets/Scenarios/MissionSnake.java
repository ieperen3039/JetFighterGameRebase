package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.Engine.GameState.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.GridMesh;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 2-3-2018.
 */
public class MissionSnake extends GameState {

    private static final float PILLAR_MIDDLE = 25f;
    private static final float PILLAR_RING = 30f;
    private static final float PILLAR_BASE = 40f;
    private static final float PILLAR_BASE_HEIGHT = 20;
    private static final float LEVEL_HEIGHT = 100f;
    private static final int LEVEL_SQUARE_COUNT = 20;
    private static final float LEVEL_SQUARE_SIZE = 50f;

    private static final PosVector VEC_GROUND = new PosVector(PILLAR_BASE, PILLAR_BASE, 0);
    private static final PosVector VEC_RING = new PosVector(PILLAR_RING, PILLAR_RING, PILLAR_BASE_HEIGHT / 2);
    private static final PosVector VEC_MIDDLE = new PosVector(PILLAR_MIDDLE, PILLAR_MIDDLE, PILLAR_BASE_HEIGHT);

    private static final Color4f WORLD_COLOR = Color4f.WHITE;
    private static final Material WORLD_MATERIAL = Material.ROUGH;

    private static final Shape gamePillar = makePillar();
    private static final Shape gameFloor = makeFloorTiles();

    public MissionSnake(Player player, GameTimer time) {
        super(player, time);
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        final DirVector g = new DirVector(0, 0, -9.81f);
        return g.scale(entity.getMass(), g);
    }

    @Override
    public Color4f fogColor() {
        return new Color4f(0.2f, 0.2f, 0.2f, 0f);
    }

    @Override
    public void buildScene() {
        staticEntities.add(new StaticObject(gameFloor, WORLD_MATERIAL, WORLD_COLOR));

        for (int x = 0; x < LEVEL_SQUARE_COUNT; x++) {
            for (int y = 0; y < LEVEL_SQUARE_COUNT; y++) {
                final Vector offSet = new DirVector(LEVEL_SQUARE_SIZE * x, LEVEL_SQUARE_SIZE * y, 0);
                staticEntities.add(new StaticObject(gamePillar, WORLD_MATERIAL, WORLD_COLOR, offSet, 1));
            }
        }
    }

    /**
     * generates a pillar shape, with the predefined parameters
     */
    private static Shape makePillar(){
        CustomShape frame = new CustomShape();

        squareRing(frame, VEC_GROUND, VEC_RING);
        squareRing(frame, VEC_RING, VEC_MIDDLE);

        PosVector roof = new PosVector(0, 0, LEVEL_HEIGHT);
        // create new vertices because we shouldn't mess with passed parameters
        PosVector top = roof.sub(VEC_GROUND, new PosVector());
        PosVector upper = roof.sub(VEC_RING, new PosVector());

        squareRing(frame, roof, top);
        squareRing(frame, top, upper);

        return frame.wrapUp();
    }

    /**
     * creates a ring/band by mirroring the given vectors in XZ and YZ planes.
     * @param frame the frame where these new quads be bestowed upon
     * @param lower the vector with the lowest z-value
     * @param upper the '' highest z-value
     */
    private static void squareRing(CustomShape frame, PosVector lower, PosVector upper) {
        frame.addQuad(lower, upper);
        frame.addQuad(lower.mirrorX(new PosVector()), upper.mirrorX(new PosVector()));
        frame.addMirrorQuad(lower, upper, lower.mirrorX(new PosVector()), upper.mirrorX(new PosVector()));
    }

    /**
     * generate a pattern of squares, implemented as a GridMesh for improved collision detection performance
     * @return a flat plane with tiling to allow pillars to be placed on every (x * SQUARE_SIZE, y * SQUARE_SIZE) coordinate
     */
    private static Shape makeFloorTiles(){
        PosVector[][] world = new PosVector[2 * LEVEL_SQUARE_COUNT][2 * LEVEL_SQUARE_COUNT];

        for (int x = 0; x < LEVEL_SQUARE_COUNT; x++) {
            for (int y = 0; y < LEVEL_SQUARE_COUNT; y++) {
                addPoint(world, x, y, false, false);
                addPoint(world, x, y, true, false);
                addPoint(world, x, y, false, true);
                addPoint(world, x, y, true, true);
            }
        }

        return new GridMesh(world);
    }

    private static void addPoint(PosVector[][] world, int x, int y, boolean xs, boolean ys) {
        int xsi = xs ? 1 : 0;
        int ysi = ys ? 1 : 0;

        final PosVector vecGround = new PosVector(VEC_GROUND);
        if (xs) vecGround.mirrorX(vecGround);
        if (ys) vecGround.mirrorY(vecGround);

        final float xPos = (x + xsi) * LEVEL_SQUARE_SIZE;
        final float yPos = (y + ysi) * LEVEL_SQUARE_SIZE;
        vecGround.add(xPos, yPos, 0);

        world[(2 * x) + xsi][(2 * y) + ysi] = vecGround;
    }
}
