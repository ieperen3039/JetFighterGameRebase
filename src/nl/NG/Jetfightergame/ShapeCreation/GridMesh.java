package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Quad;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Extreme;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collections;
import java.util.Iterator;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author Geert van Ieperen
 *         created on 13-11-2017.
 */
public class GridMesh implements Shape {

    private final Quad[][] planeGrid;
    private final Mesh graphicalGrid;

    private final int xSize;
    private final int ySize;

    /** the absolute minimum of all vectors */
    private final Vector3fc minimumTranspose;
    /** the multiplier to normalize a vector to the grid */
    private final Vector3fc normalizingScalar;

    /**
     * creates a mesh based on the vector-grid defined as grid[x][y]. The vectors of this grid should be strictly separated:
     * (a - 0.5) < grid[a][b].x < (a + 0.5) && (b - 0.5) < grid[a][b].y < (b + 0.5)
     * @param grid a matrix of vectors that form a heightmap-like grid.
     */
    public GridMesh(PosVector[][] grid) {
        xSize = grid.length - 1;
        ySize = grid[0].length - 1;

        // the absolute minimum of all vectors
        minimumTranspose = new Vector3f(grid[0][0]).add(-1f, -1f, 0).mul(1, 1, 0).toImmutable();
        // the absolute maximum of all vectors
        final Vector3f maxVector = new Vector3f(grid[xSize][ySize]).add(1f, 1f, 0);
        normalizingScalar = new Vector3f(1, 1, 1).div(maxVector.mul(xSize, ySize, 0)).toImmutable();

        planeGrid = new Quad[xSize][ySize];
        CustomShape frame = new CustomShape(new PosVector(0, 0, Float.NEGATIVE_INFINITY));

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                final PosVector A = grid[x][y];
                final PosVector B = grid[x + 1][y];
                final PosVector C = grid[x + 1][y + 1];
                final PosVector D = grid[x][y + 1];

                frame.addQuad(A, B, C, D, DirVector.zVector());
                planeGrid[x][y] = new Quad(A, B, C, D, DirVector.zVector());
            }
        }

        this.graphicalGrid = frame.asMesh();

        Logger.print("created Grid [ " + xSize + " x " + ySize + " ]");
    }

    public GridMesh(float[][] heightMap, float xStep, float yStep) {
        this(readHeightmap(heightMap, xStep, yStep));
    }

    private static PosVector[][] readHeightmap(float[][] heightMap, float xStep, float yStep) {
        int xSize = heightMap.length;
        int ySize = heightMap[0].length;
        PosVector[][] map = new PosVector[xSize][ySize];

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                map[x][y] = new PosVector(x*xStep, y*yStep, heightMap[x][y]);
            }
        }
        return map;
    }

    @Override
    public Iterable<? extends Plane> getPlanes() {
        return (Iterable<Plane>) PlaneIterator::new;
    }

    @Override
    public Iterable<PosVector> getPoints() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType,unchecked
        return Collections.EMPTY_LIST;
    }

    /**
     * much more efficient implementation than the default. should be used for all grid-based environments
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1
     * otherwise, it provides a collision object about the first collision with this shape
     */
    public Collision getCollision(PosVector linePosition, DirVector direction, PosVector endPoint) {

        Vector3f mappedSource = linePosition.sub(minimumTranspose, new Vector3f()).mul(normalizingScalar);
        Vector3f mappedDest = endPoint.sub(minimumTranspose, new Vector3f()).mul(normalizingScalar);

        int leastX = (int) Math.floor(min(mappedSource.x(), mappedDest.x()));
        int mostX = (int) Math.ceil(max(mappedSource.x(), mappedDest.x()));
        int leastY = (int) Math.floor(min(mappedSource.y(), mappedDest.y()));
        int mostY = (int) Math.ceil(max(mappedSource.y(), mappedDest.y()));

        leastX = max(0, leastX - 1);
        leastY = max(0, leastY - 1);
        mostX = min(xSize, mostX + 1);
        mostY = min(ySize, mostY + 1);

        Extreme<Collision> firstCrash = new Extreme<>(false);

        // for all planes in the region of the target line, and planes are coordinated by their minimum coord
        for(int x = leastX; x < mostX; x++){
            for(int y = leastY; y < mostY; y++){

                Quad target = planeGrid[x][y];
                firstCrash.check(target.getCollisionWith(linePosition, direction, endPoint));
            }
        }

        return firstCrash.get();
    }

    @Override
    public void render(GL2.Painter lock) {
        graphicalGrid.render(lock);
    }

    @Override
    public void dispose() {
        graphicalGrid.dispose();
    }

    private class PlaneIterator implements Iterator<Plane> {
        int x = 0;
        int y = 0;

        @Override
        public boolean hasNext() {
            return (x == xSize) && (y == ySize);
        }

        @Override
        public Plane next() {
            x++;
            if (x == xSize) {
                x = 0;
                y++;
            }
            return planeGrid[x][y];
        }
    }
}
