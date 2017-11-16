package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.GameObjects.Hitbox.Collision;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Plane;
import nl.NG.Jetfightergame.GameObjects.Surfaces.Triangle;
import nl.NG.Jetfightergame.Tools.OpenSimplexNoise;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *         created on 13-11-2017.
 */
public class SimplexGrid implements Shape {
    private final Triangle[][] alphaGrid;
    private final Triangle[][] betaGrid;

    // difference between minimum and maximum of OpenSimplexNoise
    private static final double SIM_VAR = 2 * 0.852;

    private final int xSize;
    private final int ySize;

    public SimplexGrid(PosVector[][] grid) {
        xSize = grid.length - 1;
        ySize = grid[0].length - 1;

        alphaGrid = new Triangle[xSize][ySize];
        betaGrid = new Triangle[xSize][ySize];

        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                final PosVector A = grid[x][y];
                final PosVector B = grid[x + 1][y];
                final PosVector C = grid[x][y + 1];
                final PosVector D = grid[x + 1][y + 1];
                alphaGrid[x][y] = new Triangle(A, B, C, Plane.getNormalVector(A, B, C, DirVector.Z));
                betaGrid[x][y] = new Triangle(D, B, C, Plane.getNormalVector(A, B, C, DirVector.Z));
            }
        }
        Toolbox.print("created SimplexGrid [ "+ xSize +" x "+ ySize +" ]");
    }

    /**
     * generates an openSimplex grid with z = [0, 1], dx = 1, centered around (0, 0, 0).
     * Actual dimensions may be adapted using scaling
     * @param scatter the fraction of planeSize that one point may diverge from its location
     * @param rows number of rows of the grid, or -1 if infinite
     * @param pointDensity an arbitrary number linear to the number of points between two hilltops
     */
    static Shape buildTerrain(OpenSimplexNoise noise, float scatter, int rows, float pointDensity){
        if (rows < 0) throw new UnsupportedOperationException("no support for inifinite worlds yet");

        final PosVector[][] grid = new PosVector[rows][rows];
        for(int x = 0; x < rows; x++){
            for(int y = 0; y < rows; y++){
                // comparable to x and y
                double xCoord = ((getRandom(scatter) + x) - rows/2);
                double yCoord = ((getRandom(scatter) + y) - rows/2);

                final double height = ((noise.eval(xCoord/pointDensity, yCoord/pointDensity) / SIM_VAR) + 0.5);
                grid[x][y] = new PosVector(xCoord, yCoord, height);
            }
        }

        return new SimplexGrid(grid);
    }

    /** @return random between -scatter and scatter */
    private static double getRandom(float scatter) {
        return (2 * Settings.random.nextDouble() - 1f) * scatter;
    }

    @Override
    public Stream<? extends Plane> getPlanes() {
        Stream.Builder<Plane> result = Stream.builder();
        for (int x = 0; x < xSize; x++){
            for (int y = 0; y < ySize; y++){
                result.add(alphaGrid[x][y]);
                result.add(betaGrid[x][y]);
            }
        }
        return result.build();
    }

    @Override
    public Collection<PosVector> getPoints() {
        Collection<PosVector> list = new LinkedList<>();
        getPlanes()
                .map(Plane::getVertices)
                .flatMap(Collection::stream)
                .forEach(list::add);
        return list;
    }

    @Override
    public void draw(GL2 gl) {
        gl.beginEnvironment(GL2.GL_TRIANGLES);
        {
            drawTriangles(gl);
        }
        gl.endEnvironment();
    }

    @Override
    public void drawTriangles(GL2 gl) {
//        int xMin = 0;
//        int yMin = 0;
//        int xMax = xSize;
//        int yMax = ySize;
//
//        while ((xMax - xMin) > fieldDivisionSize){
//            while ((yMax - yMin) > fieldDivisionSize){
//            }
//        }
        // todo: make mesh out of it and store it in graphics card
        for (int x = 0; x < xSize; x++){
            for (int y = 0; y < ySize; y++){
                alphaGrid[x][y].drawRaw(gl);
                betaGrid[x][y].drawRaw(gl);
            }
        }
    }

    @Override
    public void drawQuads(GL2 gl) {
        // return;
    }

    /**
     * much more efficient implementation than the default. should be used for all grid-based environments
     * @param linePosition a position vector on the line in local space
     * @param direction    the direction vector of the line in local space
     * @param endPoint     the endpoint of this vector, defined as {@code linePosition.add(direction)}
     * @return {@code null} if it does not hit with direction scalar < 1
     * otherwise, it provides a collision object about the first collision with this shape
     */
    public Collision getMaximumMovement(PosVector linePosition, DirVector direction, PosVector endPoint) {
        int leastX = (int) Math.floor(Math.min(linePosition.x(), endPoint.x()));
        int mostX = (int) Math.ceil(Math.max(linePosition.x(), endPoint.x()));
        int leastY = (int) Math.floor(Math.min(linePosition.y(), endPoint.y()));
        int mostY = (int) Math.ceil(Math.max(linePosition.y(), endPoint.y()));

        Collision currLeast = null;

        // for all planes in the region of the target line, and planes are coordinated by their minimum coord
        for(int x = leastX; x <= mostX + 1; x++){
            for(int y = leastY; x <= mostY + 1; y++){
                currLeast = getCollision(linePosition, direction, endPoint, currLeast, alphaGrid[x][y]);
                currLeast = getCollision(linePosition, direction, endPoint, currLeast, betaGrid[x][y]);
            }
        }

        return currLeast;
    }

    /**
     * given linepiece parameters, determines which collision is earlier: the currentleast or the one caused by
     * the line that hits the target.
     * @param target a triangle that may or may not be hit by the line
     * @return the earliest collision of currLeast and the new collision
     */
    private Collision getCollision(PosVector linePosition, DirVector direction, PosVector endPoint, Collision currentLeast, Triangle target) {
        Collision hit = target.getHitvector(linePosition, direction, endPoint);
        if (hit != null) {
            if (currentLeast == null || hit.compareTo(currentLeast) < 0) {
                currentLeast = hit;
            }
        }
        return currentLeast;
    }
}
