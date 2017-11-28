package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Tools.OpenSimplexNoise;
import nl.NG.Jetfightergame.Vectors.Color4f;

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

    private Shape topGrid;
    private Shape bottomGrid;

    public SimplexCave() {
        OpenSimplexNoise noiseBottom = new OpenSimplexNoise(SEED);
        OpenSimplexNoise noiseTop = new OpenSimplexNoise(SEED + 1);

        topGrid = SimplexGrid.buildTerrain(noiseTop, 0.3f, ROWS, PLANE_SIZE);
        bottomGrid = SimplexGrid.buildTerrain(noiseBottom, 0.3f, ROWS, PLANE_SIZE);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean takeStable) {
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
    public void toLocalSpace(MatrixStack ms, Runnable action, boolean takeStable) {
        action.run();
    }

    /**
         * checks the movement of the hitpoints of this object agains the planes of 'other'.
         * @param other an object that may hit this object
         */
    public void checkCollisionWith(Touchable other) {

    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(GROUND, Color4f.GREY);
    }

    @Override
    public String toString() {
        return "world SimplexCave";
    }
}
