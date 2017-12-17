package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.GameObjects.Hitbox.Collision;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Primitives.Surfaces.Triangle;
import nl.NG.Jetfightergame.ShapeCreators.Mesh;
import nl.NG.Jetfightergame.ShapeCreators.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Geert van Ieperen
 *         created on 13-11-2017.
 */
public class GridMesh implements Shape {
    private final Triangle[][] alphaGrid;
    private final Triangle[][] betaGrid;

    private final int xSize;
    private final int ySize;
    private final Mesh mesh;

    public GridMesh(PosVector[][] grid) {
        xSize = grid.length - 1;
        ySize = grid[0].length - 1;

        alphaGrid = new Triangle[xSize][ySize];
        betaGrid = new Triangle[xSize][ySize];

        List<Mesh.Face> faces = new ArrayList<>(2 * xSize * ySize);
        List<PosVector> vertices = new ArrayList<>(xSize * ySize);
        List<DirVector> normals = new ArrayList<>(2 * xSize * ySize);

        // iterate y-first
        for(int x = 0; x < xSize; x++){
            for(int y = 0; y < ySize; y++){
                // only one due to redundancy of the loop. vertices are added y-first
                vertices.add(grid[x][y]);

                createAndSetTriangles(x, y, grid, faces, normals);
            }
            // we have 1 more row of vertices than we have faces
            vertices.add(grid[x][ySize]);
        }
        // we have 1 more row of vertices than we have faces
        vertices.addAll(Arrays.asList(grid[xSize]));


        mesh = new Mesh(vertices, normals, faces);

        Toolbox.print("created Grid [ "+ xSize +" x "+ ySize +" ]");
    }

    /**
     * add triangles to {@code alphaGrid} and {@code betaGrid}
     * @param x lowest x coordinate of these two triangles
     * @param y lowest y coordinate of these two triangles
     * @param grid a grid of positionVectors, defining a surface
     */
    private void createAndSetTriangles(int x, int y, PosVector[][] grid, List<Mesh.Face> faces, List<DirVector> normals) {
        final PosVector A = grid[x][y];
        final PosVector B = grid[x + 1][y];
        final PosVector C = grid[x][y + 1];
        final PosVector D = grid[x + 1][y + 1];

        alphaGrid[x][y] = new Triangle(A, B, C, Plane.getNormalVector(A, B, C, DirVector.zVector()));
        int alphaNormal = normals.size();
        normals.add(alphaGrid[x][y].getNormal());
        faces.add(new Mesh.Face(
                index(x, y), //A
                index(x + 1, y), //B
                index(x, y + 1), //C
                alphaNormal) //normal 1
        );

        betaGrid[x][y] = new Triangle(D, B, C, Plane.getNormalVector(D, B, C, DirVector.zVector()));
        int betaNormal = normals.size();
        normals.add(betaGrid[x][y].getNormal());
        faces.add(new Mesh.Face(
                index(x + 1, y + 1), //D
                index(x + 1, y), //B
                index(x, y + 1), //C
                betaNormal) //normal 2
        );
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

    /**
     * numbers a grid (x, y) y-first
     * @return index of this coordinate
     */
    private int index(int x, int y){
        return x * xSize + y;
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
        Collection<PosVector> list = new ArrayList<>();
        getPlanes()
                .map(Plane::getVertices)
                .flatMap(Collection::stream)
                .forEach(list::add);
        return list;
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

    @Override
    public void render(GL2.Painter lock) {
        mesh.render(lock);
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
