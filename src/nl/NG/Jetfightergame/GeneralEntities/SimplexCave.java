package nl.NG.Jetfightergame.GeneralEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.RigidBody;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Rendering.Shaders.Material;
import nl.NG.Jetfightergame.ShapeCreators.GridMesh;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.OpenSimplexNoise;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 13-11-2017.
 */
public class SimplexCave implements Touchable {

    private static final long SEED = 2;
    private static final int WORLD_SIZE = 200;
    private static final int PLANE_SIZE = 4;
    public static final int ROWS = WORLD_SIZE / PLANE_SIZE;
    /** horizontal scaling */
    private static final float STRETCH = 20;
    /** vertical scaling */
    private static final float DEPTH = 80;
    private static final Material GROUND = Material.ROUGH;
    private static final float MERGE_FACTOR = 0.25f;
    // difference between minimum and maximum of OpenSimplexNoise
    private static final float SIM_VAR = 2 * 0.852f;

    private Collection<Vector2f> collisions;

    private Shape topGrid;
    private Shape bottomGrid;

    public SimplexCave() {
        OpenSimplexNoise noiseBottom = new OpenSimplexNoise(SEED);
        OpenSimplexNoise noiseTop = new OpenSimplexNoise(SEED + 1);

        topGrid = buildTerrain(noiseTop, 0.3f, ROWS, PLANE_SIZE);
        bottomGrid = buildTerrain(noiseBottom, 0.3f, ROWS, PLANE_SIZE);

        collisions = new ArrayList<>();
    }

    /**
     * generates an openSimplex grid with z = [0, 1], dx = 1, centered around (0, 0, 0).
     * Actual dimensions may be adapted using scaling
     * @param scatter the fraction of planeSize that one point may diverge from its location
     * @param rows number of rows of the grid, or -1 if infinite
     * @param pointDensity an arbitrary number linear to the number of points between two hilltops
     */
    private static Shape buildTerrain(OpenSimplexNoise noise, float scatter, int rows, float pointDensity){
        if (rows < 0) throw new UnsupportedOperationException("no support for inifinite worlds yet");

        final PosVector[][] grid = new PosVector[rows][rows];
        for(int x = 0; x < rows; x++){
            for(int y = 0; y < rows; y++){
                // comparable to x and y
                float xCoord = ((getRandom(scatter) + x) - rows/2);
                float yCoord = ((getRandom(scatter) + y) - rows/2);

                final float height =  ((float) noise.eval(xCoord/pointDensity, yCoord/pointDensity) / SIM_VAR + 0.5f);
                grid[x][y] = new PosVector(xCoord, yCoord, height);
            }
        }

        return new GridMesh(grid);
    }

    /** @return random between -scatter and scatter */
    private static float getRandom(float scatter) {
        return (2 * Settings.random.nextFloat() - 1f) * scatter;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.scale(STRETCH, STRETCH, DEPTH);
            ms.pushMatrix();
            {
                ms.translate(0, 0, -1 + MERGE_FACTOR);
                action.accept(topGrid);
            }
            ms.popMatrix();
            // set upside-down
            ms.scale(1, 1, -1);
            ms.translate(0, 0, -1 + MERGE_FACTOR);
            action.accept(bottomGrid);
        }
        ms.popMatrix();
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        action.run();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(GROUND, Color4f.GREY);
    }

    @Override
    public void acceptCollision(Collision cause) {
        final PosVector pos = cause.hitPos;
        collisions.add(new Vector2f(
                pos.x(), pos.y()
        ));
    }

    @Override
    public RigidBody getFinalCollision(float deltaTime) {
        collisions.forEach(a -> {});
        collisions.clear();
        return new RigidBody(this);
    }

    @Override
    public String toString() {
        return "world SimplexCave";
    }
}
